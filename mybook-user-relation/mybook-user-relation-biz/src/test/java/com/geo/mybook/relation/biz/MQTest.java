package com.geo.mybook.relation.biz;


import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.relation.biz.constant.MQConstants;
import com.geo.mybook.relation.biz.domain.dto.CountFollowUnFollowMQDTO;
import com.geo.mybook.relation.biz.domain.dto.FollowUserMQDTO;
import com.geo.mybook.relation.biz.domain.dto.UnfollowUserMQDTO;
import com.geo.mybook.relation.biz.enums.FollowUnFollowTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;

import static com.geo.mybook.relation.biz.constant.MQConstants.*;

/*
creator：AZERL7
createTime：10:59
*/
@Slf4j
@SpringBootTest
public class MQTest {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 用来测试限流
     */
    @Test
    void testBatchSendMQ(){
        //1、模拟发送1000条消息，测试RateLimiter
        for(Long i=0L;i<10;i+=1){
            FollowUserMQDTO followUserMqDTO = FollowUserMQDTO.builder()
                    .userId(i)
                    .followUserId(i)
                    .createTime(LocalDateTime.now())
                    .build();
            //2、构建消息体准备发送
            Message<String> message= MessageBuilder
                    .withPayload(JsonUtils.toJsonString(followUserMqDTO))
                    .build();

            // 冒号连接添加tag
            String destination=TOPIC_FOLLOW_OR_UNFOLLOW+":"+TAG_FOLLOW;

            log.info("==> 发送到第 {} 条消息",i.toString());

            final Long finalI = i;//要复制一遍是，因为java的匿名内部类对外部的变量访问规则要求该变量符合 effectively final
            //使用final修饰即可，如果没有修饰，并且没有修改过，编译器会隐式添加final
            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("==> 发送 MQ 消息成功，第 {} 条", finalI.toString());
                }

                @Override
                public void onException(Throwable throwable) {
                    log.info("==> 发送 MQ 消息失败，第 {} 条",finalI.toString());
                }
            });

        }
    }


    /**
     * 用来测试顺序消息
     */
    @Test
    void testSendFollowUnfollowMQ(){
        Long userId=27L;//操作者id
        Long targetUserId=100L;
        String hashKey=String.valueOf(userId);
        for(int i=0;i<10;i+=1){
            if(i%2==0){//偶数发关注
                log.info("关注消息发送 {}",i);
                //发送
                FollowUserMQDTO followUserMQDTO= FollowUserMQDTO.builder()
                        .userId(userId)
                        .followUserId(targetUserId)
                        .createTime(LocalDateTime.now())
                        .build();

                Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMQDTO)).build();
                String destination= TOPIC_FOLLOW_OR_UNFOLLOW+":"+TAG_FOLLOW;
                SendResult sendResult=rocketMQTemplate.syncSendOrderly(destination,message,hashKey);
                log.info("==> 关注消息发送完毕 {}",sendResult);
            }else{//奇数取关
                log.info("取关消息发送 {}",i);
                UnfollowUserMQDTO unfollowUserMQDTO= UnfollowUserMQDTO.builder()
                        .userId(userId)
                        .unfollowUserId(targetUserId)
                        .createTime(LocalDateTime.now())
                        .build();
                Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(unfollowUserMQDTO)).build();

                String destination=TOPIC_FOLLOW_OR_UNFOLLOW+":"+TAG_UNFOLLOW;
                SendResult sendResult=rocketMQTemplate.syncSendOrderly(destination,message,hashKey);
                log.info("==> 取关消息发送完毕 {}",sendResult);
            }
        }

    }

    /**
     * 测试计数服务中的聚合消息功能
     */
    @Test
    void testSendCountFollowUnfollow(){
        for(int i=0;i<3200;i+=1){
            CountFollowUnFollowMQDTO countFollowUnFollowMQDTO=CountFollowUnFollowMQDTO.builder()
                    .userId(i+1L)
                    .targetUserId(100000000000L)
                    .type(FollowUnFollowTypeEnum.FOLLOW.getCode())
                    .build();
            org.springframework.messaging.Message<String> message=MessageBuilder
                    .withPayload(JsonUtils.toJsonString(countFollowUnFollowMQDTO))
                    .build();

            rocketMQTemplate.asyncSend(TOPIC_COUNT_FANS, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("==> 计数消息服务发送成功，sendResult {}",sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("==> 计数服务发送失败，throwable",throwable);
                }
            });

            rocketMQTemplate.asyncSend(TOPIC_COUNT_FOLLOWING, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("==> 【计数服务：关注数】 MQ 发送成功，sendResult {}",sendResult);
                }
    
                @Override
                public void onException(Throwable throwable) {
                    log.error("==> 【计数服务：关注数】 MQ 发送失败，throwable ",throwable);
                }
            });
        }
    }
}
