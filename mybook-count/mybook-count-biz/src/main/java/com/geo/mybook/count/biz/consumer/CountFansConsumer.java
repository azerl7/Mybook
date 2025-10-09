package com.geo.mybook.count.biz.consumer;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.domain.dto.CountFollowUnfollowMQDTO;
import com.geo.mybook.count.biz.enums.FollowUnfollowTypeEnum;
import com.github.phantomthief.collection.BufferTrigger;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.geo.framework.common.util.Constants.COUNT_USER_KEY_PREFIX;
import static com.geo.framework.common.util.Constants.FIELD_FANS_TOTAL;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_FANS;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_FANS_2_DB;

/*
creator：AZERL7
createTime：10:47
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_FANS,
        topic=TOPIC_COUNT_FANS
)
public class CountFansConsumer implements RocketMQListener<String> {

    //buffer trigger
    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条，因为聚合之后的处理也需要时间，这个时候可能也有消息需要处理，所以最大容量需要大一些
            .linger(Duration.ofSeconds(1)) // 多久聚合一次，时间窗口
            .setConsumerEx(this::consumeMessage)
            .build();

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);//每次处理消息入队即可
    }

    private void consumeMessage(List<String> body){
        log.info("==> 聚合消息，size {}",body.size());
        log.info("==> 聚合消息，{}", JsonUtils.toJsonString(body));
        //0、转化为对象
        List<CountFollowUnfollowMQDTO> countFollowUnfollowMQDTOS=body.stream()
                .map(obj->
                    JsonUtils.parseObject(obj,CountFollowUnfollowMQDTO.class)
                )
                .toList();
        //1、按照目标用户进行分类（因为消息聚合的消息可能来自不同的用户，需要进行分类）
        Map<Long, List<CountFollowUnfollowMQDTO>> groupMap = countFollowUnfollowMQDTOS.stream()
                .collect(Collectors.groupingBy(CountFollowUnfollowMQDTO::getTargetUserId));

        //2、按组汇总，统计出最终计数
        //以key为目标id，value为最终计数
        Map<Long,Integer> countMap= MapUtil.newHashMap();
        for(Map.Entry<Long,List<CountFollowUnfollowMQDTO>> entry:groupMap.entrySet()){
            List<CountFollowUnfollowMQDTO> list=entry.getValue();
            int finalCount=0;
            for(CountFollowUnfollowMQDTO countFollowUnfollowMQDTO:list){
                Integer type=countFollowUnfollowMQDTO.getType();
                FollowUnfollowTypeEnum followUnfollowTypeEnum = FollowUnfollowTypeEnum.valueOf(type);
                if(ObjectUtil.isNull(followUnfollowTypeEnum))continue;
                switch(followUnfollowTypeEnum){
                    case FOLLOW -> finalCount+=1;
                    case UNFOLLOW -> finalCount-=1;
                }
            }
            countMap.put(entry.getKey(), finalCount);
        }
        log.info("## 聚合后的数据：{}",JsonUtils.toJsonString(countMap));
        countMap.forEach((k,v)->{
            String redisKey=COUNT_USER_KEY_PREFIX+k;
            boolean isExisted=stringRedisTemplate.hasKey(redisKey);
            //存在才更新
            //因为缓存设置有过期时间，考虑到过期之后缓存会被删除，这里判断一下，存在才更新，不存在就不做操作
            //而初始化工作放在查询计数来做，为什么？前面有说过，这是高并发场景，要尽量减少操作次数，
            // 所以在需要的地方刷新就行了，（这里不需要吗？因为这里用户看不到所以就没必要在redis刷新）
            if(isExisted){
                stringRedisTemplate.opsForHash().increment(redisKey,FIELD_FANS_TOTAL,v);
            }
        });
        //todo: 发送 mq 消息写入数据库


        Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(countMap)).build();

        rocketMQTemplate.asyncSend(TOPIC_COUNT_FANS_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：粉丝数入库】MQ 消息发送成功，SendResult: {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("==> 【计数服务：粉丝数入库】MQ发送异常：",throwable);
            }
        });

    }
}
