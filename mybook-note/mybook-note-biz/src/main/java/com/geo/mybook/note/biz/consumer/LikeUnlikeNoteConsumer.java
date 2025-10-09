package com.geo.mybook.note.biz.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.note.biz.config.ThreadPoolConfig;
import com.geo.mybook.note.biz.domain.dto.LikeUnLikeNoteMQDTO;
import com.geo.mybook.note.biz.domain.po.NoteLike;
import com.geo.mybook.note.biz.mapper.NoteLikeMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.apache.rocketmq.common.message.Message;

import java.time.LocalDateTime;

import static com.geo.mybook.note.biz.constant.MQConstants.*;

/*
creator：AZERL7
createTime：11:25
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_LIKE_OR_UNLIKE,
        topic=TOPIC_LIKE_OR_UNLIKE,
        consumeMode = ConsumeMode.ORDERLY //设置为顺序消费模式
)
public class LikeUnlikeNoteConsumer implements RocketMQListener<Message> {

    @Resource
    private NoteLikeMapper noteLikeMapper;

    //每秒创建5000个令牌
    private final RateLimiter rateLimiter=RateLimiter.create(5000);
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        rateLimiter.acquire();
        String bodyJsonStr=new String(message.getBody());
        String tags=message.getTags();
        log.info("==> LikeUnLikeNoteConsumer 消费了消息 {}，tags {}",bodyJsonStr,tags);
        if(ObjectUtil.equal(tags,TAG_LIKE)){
            handleLikeNoteTagMessage(bodyJsonStr);
        }else if(ObjectUtil.equal(tags,TAG_UNLIKE)){
            handleUnLikeNoteTagMessage(bodyJsonStr);
        }
    }

    /**
     *  笔记点赞
     * @param body 消息体
     */
    private void handleLikeNoteTagMessage(String body){
        LikeUnLikeNoteMQDTO likeUnLikeNoteMQDTO= JsonUtils.parseObject(body,LikeUnLikeNoteMQDTO.class);
        if(ObjectUtil.isNull(likeUnLikeNoteMQDTO)){
            log.error("## 【json字符串转换】 json转字符串失败 ：LikeUnlikeNoteConsumer 50 行");
            return;
        }
        Long userId=likeUnLikeNoteMQDTO.getUserId();
        Long noteId=likeUnLikeNoteMQDTO.getNoteId();
        Integer type=likeUnLikeNoteMQDTO.getType();
        LocalDateTime createTime=likeUnLikeNoteMQDTO.getCreateTime();
        NoteLike noteLike= NoteLike.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();
        int count=noteLikeMapper.insertOrUpdateMybatis(noteLike);
        if(count==0) return;
        //发送计数服务mq

        org.springframework.messaging.Message<String> message= MessageBuilder
                .withPayload(body).build();

        rocketMQTemplate.asyncSend(TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数：笔记点赞】MQ 消息发送成功,SendResult {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数：笔记点赞】MQ 消息发送失败,throwable:",throwable);
            }
        });

    }


    /**
     * 笔记点赞
     * @param body 消息体
     */
    private void handleUnLikeNoteTagMessage(String body){
        //1、获取信息
        LikeUnLikeNoteMQDTO likeUnLikeNoteMQDTO=JsonUtils.parseObject(body,LikeUnLikeNoteMQDTO.class);
        if(ObjectUtil.isNull(likeUnLikeNoteMQDTO)){
            log.error("## 【json字符串转换】 json字符串转换失败 LikeUnLikeNoteConsumer 86 行");
            return;
        }
        //2、数据库操作
        Long userId=likeUnLikeNoteMQDTO.getUserId();
        Long noteId=likeUnLikeNoteMQDTO.getNoteId();
        Integer type=likeUnLikeNoteMQDTO.getType();
        LocalDateTime createTime=likeUnLikeNoteMQDTO.getCreateTime();
        NoteLike note=NoteLike.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();
        int count=noteLikeMapper.update2UnlikeByUserIdAndNoteId(note);
        if(count==0)return;
        //3、发送计数mq消息
        org.springframework.messaging.Message<String> message=MessageBuilder
                .withPayload(body).build();

        rocketMQTemplate.asyncSend(TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数：笔记取消点赞】mq 消息发送成功，sendResult：{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数：笔记取消点赞】mq消息发送失败，throwable：",throwable);
            }
        });

    }

}
