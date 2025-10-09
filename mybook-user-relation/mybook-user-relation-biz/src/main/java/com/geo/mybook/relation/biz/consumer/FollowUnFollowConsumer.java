package com.geo.mybook.relation.biz.consumer;


/*
creator：AZERL7
createTime：22:43
*/
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.DateUtils;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.relation.biz.constant.MQConstants;
import com.geo.mybook.relation.biz.domain.dto.CountFollowUnFollowMQDTO;
import com.geo.mybook.relation.biz.domain.dto.FollowUserMQDTO;
import com.geo.mybook.relation.biz.domain.dto.UnfollowUserMQDTO;
import com.geo.mybook.relation.biz.domain.po.Fans;
import com.geo.mybook.relation.biz.domain.po.Following;
import com.geo.mybook.relation.biz.enums.FollowUnFollowTypeEnum;
import com.geo.mybook.relation.biz.mapper.FansMapper;
import com.geo.mybook.relation.biz.mapper.FollowingMapper;
//import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.apache.rocketmq.common.message.Message;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.geo.framework.common.util.Constants.USER_FANS_KEY_PREFIX;
import static com.geo.mybook.relation.biz.constant.MQConstants.*;


@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_FOLLOW_OR_UNFOLLOW,//Group组
        topic= TOPIC_FOLLOW_OR_UNFOLLOW
//        messageModel = MessageModel.BROADCASTING
)//使用默认的点对点模式进行消费，因为需要消费的写数据库操作只需要一个人即可
//隐藏的MessageModel=MessageModel
public class FollowUnFollowConsumer implements RocketMQListener<Message> {

    @Resource
    private FansMapper fansMapper;

    @Resource
    private FollowingMapper followingMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    //初始化令牌桶，每秒创建5000个令牌
    @Resource
    private RateLimiter rateLimiter;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        //0、获取令牌,处理一次请求（流量削峰）
        rateLimiter.acquire();//如果拿到了令牌执行后面的代码，如果没有拿到令牌，则阻塞等待拿到令牌

        String bodyJsonStr=new String(message.getBody());
        String tags=message.getTags();
        log.info("==> FollowUnFollowConsumer 消费了消息 {}，tags {}",bodyJsonStr,tags);

        //根据 MQ 标签，判断操作类型
        if(ObjectUtil.equal(tags,MQConstants.TAG_FOLLOW)){//关注
            handleFollowTagMessage(bodyJsonStr);
        }else if(ObjectUtil.equal(tags,MQConstants.TAG_UNFOLLOW)){//取关
            handleUnFollowTagMessage(bodyJsonStr);
        }
    }

    /**
     * 使用编程式事务控制数据库操作保证操作的原子性（也方便后面使用分布式事务）
     * @param bodyJsonStr 消息体json数据
     */
    private void handleFollowTagMessage(String bodyJsonStr){
        //1、获取数据
        FollowUserMQDTO followUserMQDTO= JsonUtils.parseObject(bodyJsonStr, FollowUserMQDTO.class);

        if(ObjectUtil.isNull(followUserMQDTO)){
            return;
        }

        //幂等性：通过联合索引保证
        Long userId= followUserMQDTO.getUserId();
        Long followUserId= followUserMQDTO.getFollowUserId();
        LocalDateTime createTime=followUserMQDTO.getCreateTime();

        //2、编程式事务
        boolean isSuccess=Boolean.TRUE.equals(transactionTemplate.execute(status->{
            try{
                //2.1、关注表
                int count=followingMapper.insert(
                        Following.builder()
                                .userId(userId)
                                .followingUserId(followUserId)
                                .createTime(createTime)
                                .build());

                //2.2、粉丝表
                if(count>0){
                    fansMapper.insert(Fans.builder()
                                    .userId(followUserId)
                                    .fansUserId(userId)
                                    .createTime(createTime)
                            .build());
                }
                return true;
            }catch(Exception e){
                status.setRollbackOnly();
                log.error("",e);
            }
            return false;
        }));
        log.info("## 数据库添加记录结果：{}",isSuccess);
        //3、如果数据库修改成功，更新redis中被关注着的ZSET,粉丝数量无上限，因此仅仅缓存近的粉丝，大约5000名，等往下拉的时候刷取到redis
        if(isSuccess){
            DefaultRedisScript<Long> script=new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);

            long timestamp= DateUtils.localDateTime2TimeStamp(LocalDateTime.now());
            String fansRedisKey=USER_FANS_KEY_PREFIX+followUserId;
            stringRedisTemplate.execute(script,Collections.singletonList(fansRedisKey),userId.toString(),Long.toString(timestamp));

            //todo：发送 mq 消息通知计数服务，统计关注数量
            CountFollowUnFollowMQDTO countFollowUnFollowMQDTO=CountFollowUnFollowMQDTO.builder()
                    .userId(userId)
                    .targetUserId(followUserId)
                    .type(FollowUnFollowTypeEnum.FOLLOW.getCode())
                    .build();
            sendMQ(countFollowUnFollowMQDTO);
            //todo：发送 mq 消息通知计数服务，统计粉丝数量
        }
    }

    private void handleUnFollowTagMessage(String bodyJsonStr){
        UnfollowUserMQDTO unfollowUserMQDTO=JsonUtils.parseObject(bodyJsonStr,UnfollowUserMQDTO.class);

        if(ObjectUtil.isNull(unfollowUserMQDTO))return;

        Long userId=unfollowUserMQDTO.getUserId();
        Long unfollowUserId=unfollowUserMQDTO.getUnfollowUserId();
        LocalDateTime createTime=unfollowUserMQDTO.getCreateTime();

        boolean isSuccess=Boolean.TRUE.equals(transactionTemplate.execute(status->{
            try{
                Map<String,Object> columnMap=new HashMap<>();
                columnMap.put("user_id",userId);
                columnMap.put("following_user_id",unfollowUserId);
                int count= followingMapper.deleteByMap(columnMap);
                if(count>0){
                    Map<String,Object> columnMap2=new HashMap<>();
                    columnMap2.put("user_id",unfollowUserId);
                    columnMap2.put("fans_user_id",userId);
                    fansMapper.deleteByMap(columnMap2);
                }
                return true;
            }catch(Exception ex){
                status.setRollbackOnly();//标记事务为回滚
                log.error("",ex);
            }
            return false;
        }));
        //更新缓存
        if(isSuccess){
            String fansRedisKey=USER_FANS_KEY_PREFIX+unfollowUserId;
            //为什么这里不用做验证呢，因为前面已经经过很多次验证了
            stringRedisTemplate.opsForZSet().remove(fansRedisKey,userId.toString());

            //todo :发送mq消息通知计数服务，统计粉丝数量
            //todo：发送mq消息通知计数服务，统计关注数量
            CountFollowUnFollowMQDTO countFollowUnFollowMQDTO=CountFollowUnFollowMQDTO.builder()
                    .userId(userId)
                    .targetUserId(unfollowUserId)
                    .type(FollowUnFollowTypeEnum.UNFOLLOW.getCode())
                    .build();
            sendMQ(countFollowUnFollowMQDTO);
        }
    }

    /**
     * 发送mq服务
     * @param countFollowUnFollowMQDTO 发送mq服务dto
     */
    private void sendMQ(CountFollowUnFollowMQDTO countFollowUnFollowMQDTO){
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(countFollowUnFollowMQDTO)).build();
        //有关注就一定有粉丝的操作

        // 发送 mq 消息通知计数服务：统计关注数
        rocketMQTemplate.asyncSend(TOPIC_COUNT_FOLLOWING, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 通知计数服务 mq 消息发送成功：SendResult: {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 通知计数服务 mq 消息发送失败：throwable: ",throwable);
            }
        });

        // 发送 mq 通知计数服务：统计粉丝数
        rocketMQTemplate.asyncSend(TOPIC_COUNT_FANS, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：粉丝数】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：粉丝数】MQ 发送异常: ", throwable);
            }
        });

    }
}
