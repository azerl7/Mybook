package com.geo.mybook.count.biz.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.domain.dto.CountFollowUnfollowMQDTO;
import com.geo.mybook.count.biz.enums.FollowUnfollowTypeEnum;
import feign.Body;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.geo.framework.common.util.Constants.COUNT_USER_KEY_PREFIX;
import static com.geo.framework.common.util.Constants.FIELD_FOLLOWING_TOTAL;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_FOLLOWING;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_FOLLOWING_2_DB;

/*
creator：AZERL7
createTime：10:48
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_FOLLOWING,
        topic=TOPIC_COUNT_FOLLOWING
)
public class CountFollowingConsumer implements RocketMQListener<String> {
    @Resource
    private  StringRedisTemplate stringRedisTemplate;
    @Resource
    private  RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String s) {
        log.info("==> 计数服务消费了mq消息 【计数：关注数】,{}",s);
        if(StringUtils.isBlank(s))return;

        //1、获取对象
        CountFollowUnfollowMQDTO countFollowUnfollowMQDTO= JsonUtils.parseObject(s,CountFollowUnfollowMQDTO.class);
        if(ObjectUtil.isNull(countFollowUnfollowMQDTO)){
            log.error("## 计数服务关注出现错误，字符串转换为对象失败");
            return;
        }
        Integer type=countFollowUnfollowMQDTO.getType();
        Long userId=countFollowUnfollowMQDTO.getUserId();
        String redisKey=COUNT_USER_KEY_PREFIX+userId;
        boolean isExisted=stringRedisTemplate.hasKey(redisKey);

        //2、如果redis中存在刷新redis缓存数据
        if(isExisted){
            long count=ObjectUtil.equal(type, FollowUnfollowTypeEnum.FOLLOW.getCode()) ? 1 : -1;
            stringRedisTemplate.opsForHash().increment(redisKey,FIELD_FOLLOWING_TOTAL,count);
        }


        //3、发送mq消息入库
        Message<String> message= MessageBuilder.withPayload(s).build();
        rocketMQTemplate.asyncSend(TOPIC_COUNT_FOLLOWING_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：关注数入库】MQ消息发送成功，SendResult {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：关注数入库】 MQ消息发送失败，throwable ",throwable);
            }
        });

    }
}
