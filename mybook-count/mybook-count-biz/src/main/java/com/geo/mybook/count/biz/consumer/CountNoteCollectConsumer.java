package com.geo.mybook.count.biz.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.domain.dto.AggregationCountCollectUnCollectNoteMQDTO;
import com.geo.mybook.count.biz.domain.dto.CountCollectUnCollectNoteMQDTO;
import com.geo.mybook.count.biz.enums.CollectUnCollectNoteTypeEnum;
import com.github.phantomthief.collection.BufferTrigger;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.geo.framework.common.util.Constants.COUNT_NOTE_KEY_PREFIX;
import static com.geo.framework.common.util.Constants.FIELD_COLLECT_TOTAL;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_NOTE_COLLECT;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_NOTE_COLLECT_2_DB;

/*
creator：AZERL7
createTime：10:39
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_NOTE_COLLECT,
topic = TOPIC_COUNT_NOTE_COLLECT
)
public class CountNoteCollectConsumer  implements RocketMQListener<String> {

    private final RateLimiter rateLimiter=RateLimiter.create(5000);

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private final BufferTrigger<String> bufferTrigger=BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
            .linger(Duration.ofSeconds(1))
            .setConsumerEx(this::consumerMessage)
            .build();

    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);
    }

    public void consumerMessage(List<String> bodys) {
        log.info("==> 【笔记收藏数】聚合消息,size:{}",bodys.size());
        log.info("==> 【笔记收藏数】聚合消息,{}", JsonUtils.toJsonString(bodys));

        //1、list类型转换
        List<CountCollectUnCollectNoteMQDTO> countCollectUnCollectNoteMqDTOS = bodys.stream()
                .map(body -> JsonUtils.parseObject(body, CountCollectUnCollectNoteMQDTO.class)).toList();

        //2、按照笔记id进行分组
        Map<Long,List<CountCollectUnCollectNoteMQDTO>> groupMap =countCollectUnCollectNoteMqDTOS.stream()
                .collect(Collectors.groupingBy(CountCollectUnCollectNoteMQDTO::getNoteId));

        //3、汇总出总数，统计出最终的计数
        //key作为笔记id，value为最终操作数
        List<AggregationCountCollectUnCollectNoteMQDTO> countList= Lists.newArrayList();


        for(Map.Entry<Long,List<CountCollectUnCollectNoteMQDTO>> entry:groupMap.entrySet()){
            Long noteId=entry.getKey();
            Long creatorId=null;
            List<CountCollectUnCollectNoteMQDTO> list=entry.getValue();
            int finalCount=0;
            for(CountCollectUnCollectNoteMQDTO dto:list){//去除一些无效的数据，并且计数
                Integer type=dto.getType();
                CollectUnCollectNoteTypeEnum en=CollectUnCollectNoteTypeEnum.valueOf(type);
                if(ObjectUtil.isNull(en))continue;
                switch(en){
                    case COLLECT -> finalCount+=1;
                    case UN_COLLECT -> finalCount-=1;
                }
                creatorId=dto.getCreatorId();
            }
            countList.add(AggregationCountCollectUnCollectNoteMQDTO.builder()
                    .noteId(noteId)
                    .creatorId(creatorId)
                    .count(finalCount)
                    .build());
        }
        log.info("## 【笔记收藏数】聚合后的数据:{}",JsonUtils.toJsonString(countList));

        //4、循环更新redis
        countList.forEach(item->{
            Long creatorId=item.getCreatorId();
            Long noteId=item.getNoteId();
            Integer count=item.getCount();
            String redisKey=COUNT_NOTE_KEY_PREFIX+noteId;
            //判断是否存在，如果存在则跟新，不存在交给收藏服务来初始化，和点赞一样的逻辑
            boolean isExist=stringRedisTemplate.hasKey(redisKey);
            if(isExist){
                stringRedisTemplate.opsForHash().increment(redisKey,FIELD_COLLECT_TOTAL,count);
            }
        });

        //5、发送mq消息，收藏数据入库
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(countList)).build();
        rocketMQTemplate.asyncSend(TOPIC_COUNT_NOTE_COLLECT_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：笔记收藏数据入库】MQ 消息发送成功，sendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：笔记收藏数据入库】MQ 消息发送失败，throwable:",throwable);
            }
        });
    }
}
