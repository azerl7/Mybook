package com.geo.mybook.count.biz.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.domain.dto.AggregationCountLikeUnlikeNoteMQDTO;
import com.geo.mybook.count.biz.domain.dto.CountLikeUnlikeNoteMQDTO;
import com.geo.mybook.count.biz.enums.LikeUnlikeNoteTypeEnum;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.geo.framework.common.util.Constants.*;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_NOTE_LIKE;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_NOTE_LIKE_2_DB;

/*
creator：AZERL7
createTime：14:26
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_NOTE_LIKE,
topic=TOPIC_COUNT_NOTE_LIKE
)
public class CountNoteLikeConsumer implements RocketMQListener<String> {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private final BufferTrigger<String> bufferTrigger=BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)//缓存队列最大容量
            .batchSize(1000)
            .linger(Duration.ofSeconds(1))
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);

    }

    public void consumeMessage(List<String> bodys){
        log.info("==> 【笔记点赞数】聚合消息：size:{}",bodys.size());
        log.info("==> 【笔记点赞数】聚合消息：{}", JsonUtils.toJsonString(bodys));
        //1、类型转换
        List<CountLikeUnlikeNoteMQDTO> countLikeUnlikeNoteMQDTOList=bodys.stream()
                .map(body->JsonUtils.parseObject(body,CountLikeUnlikeNoteMQDTO.class))
                .toList();
        ////////////////////////////////////////////////////////

        //2、按组汇总数据，统计最终操作计数
        Map<Long,List<CountLikeUnlikeNoteMQDTO>> groupMap=countLikeUnlikeNoteMQDTOList.stream()
                .collect(Collectors.groupingBy(CountLikeUnlikeNoteMQDTO::getNoteId));

        //使用汇总对象来存储
        List<AggregationCountLikeUnlikeNoteMQDTO> countList= Lists.newArrayList();

        for(Map.Entry<Long,List<CountLikeUnlikeNoteMQDTO>> entry : groupMap.entrySet()){
            List<CountLikeUnlikeNoteMQDTO> list=entry.getValue();
            int finalCount=0;
            Long noteId=entry.getKey();
            Long creatorId=null;
            for(CountLikeUnlikeNoteMQDTO countLikeUnlikeNoteMQDTO : list){
                Integer type=countLikeUnlikeNoteMQDTO.getType();
                LikeUnlikeNoteTypeEnum en=LikeUnlikeNoteTypeEnum.valueOf(type);
                if(ObjectUtil.isNull(en)) continue;//去除枚举为空的值
                switch(en){
                    case LIKE ->finalCount+=1;
                    case UNLIKE -> finalCount-=1;
                }
                creatorId = countLikeUnlikeNoteMQDTO.getCreatorId();
            }
            countList.add(AggregationCountLikeUnlikeNoteMQDTO.builder()
                            .noteId(noteId)
                            .creatorId(creatorId)
                            .count(finalCount)
                            .build());
        }
        log.info("## 【笔记店点赞数】 聚合后的计数数据：{}",JsonUtils.toJsonString(countList));
        //3、更新redis
        countList.forEach(item->{
            Long creatorId=item.getCreatorId();
            Long noteId=item.getNoteId();
            Integer count=item.getCount();
            String redisKey=COUNT_NOTE_KEY_PREFIX+noteId;
            boolean isExisted=stringRedisTemplate.hasKey(redisKey);
            //如果存在才更新
            //因为缓存会过期，节约资源，在查询计数的时候去初始化，因为查询的时候才会看，不存在放在数据库即可
            if(isExisted){
                stringRedisTemplate.opsForHash().increment(redisKey,FIELD_LIKE_TOTAL,count);
            }
            //更新用户维度点赞数目
            String countUserRedisKey=COUNT_USER_KEY_PREFIX+creatorId;
            boolean isCountUserIsExisted=stringRedisTemplate.hasKey(countUserRedisKey);
            if(isCountUserIsExisted){
                stringRedisTemplate.opsForHash().increment(countUserRedisKey,FIELD_LIKE_TOTAL,count);
            }
        });

        Message<String> message= MessageBuilder
                .withPayload(JsonUtils.toJsonString(countList)).build();

        rocketMQTemplate.asyncSend(TOPIC_COUNT_NOTE_LIKE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：笔记点赞数入库】MQ消息发送成功，SendResult：{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：笔记点赞数入库】MQ 消息发送失败，throwable：",throwable);
            }
        });
    }
}
