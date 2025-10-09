package com.geo.mybook.note.biz.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.note.biz.domain.dto.CollectUnCollectNoteMQDTO;
import com.geo.mybook.note.biz.domain.po.NoteCollection;
import com.geo.mybook.note.biz.mapper.NoteCollectionMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.geo.mybook.note.biz.constant.MQConstants.*;

/*
creator：AZERL7
createTime：8:50
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COLLECT_OR_UN_COLLECT,
topic=TOPIC_COLLECT_OR_UN_COLLECT,
consumeMode= ConsumeMode.ORDERLY//顺序消费避免短时间内按错，出现结果不一致的情况
)
public class CollectUnCollectNoteConsumer implements RocketMQListener<Message> {
    private final RateLimiter rateLimiter=RateLimiter.create(5000);

    @Resource
    private NoteCollectionMapper noteCollectionMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        rateLimiter.acquire();

        String bodyJsonStr=new String(message.getBody());
        String tags=message.getTags();
        log.info("==> CollectUnCollectNoteConsumer 消费了消息: {},tags{}",bodyJsonStr,tags);

        //根据标签进行收藏和取消收藏
        if(ObjectUtil.equal(tags,TAG_COLLECT)){
            handleCollectNoteTagMessage(bodyJsonStr);
        }else if(ObjectUtil.equal(tags,TAG_UN_COLLECT)){
            handleUnCollectNoteTagMessage(bodyJsonStr);
        }
    }

    /**
     * 收藏笔记
     * @param bodyJsonStr 收藏需要的信息
     */
    private void handleCollectNoteTagMessage(String bodyJsonStr){
        CollectUnCollectNoteMQDTO collectUnCollect= JsonUtils.parseObject(bodyJsonStr, CollectUnCollectNoteMQDTO.class);

        if(ObjectUtil.isNull(collectUnCollect)){
            log.info("## json字符串转换失败，或者数据不正确，传入数据 bodyJsonStr{}",bodyJsonStr);
            return;
        }

        Long userId=collectUnCollect.getUserId();
        Long noteId=collectUnCollect.getNoteId();
        LocalDateTime createTime=LocalDateTime.now();
        Integer type=collectUnCollect.getType();

        NoteCollection noteCollection= NoteCollection.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();

        int count=noteCollectionMapper.insertOrUpdateMybatis(noteCollection);
        if(count==0) return;
        //发送mq消息通知计数服务
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(bodyJsonStr).build();

        rocketMQTemplate.asyncSend(TOPIC_COUNT_NOTE_COLLECT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==>【计数：笔记点赞】MQ 发送成功，sendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==>【计数：笔记收藏】MQ 发送失败，throwable:",throwable);
            }
        });

    }

    /**
     * 取消收藏笔记
     * @param bodyJsonStr 取消收藏需要的信息
     */
    private void handleUnCollectNoteTagMessage(String bodyJsonStr){
        CollectUnCollectNoteMQDTO unCollectNoteMQDTO=JsonUtils.parseObject(bodyJsonStr, CollectUnCollectNoteMQDTO.class);
        if(ObjectUtil.isNull(unCollectNoteMQDTO)){
            log.info("## json字符串转换错误 bodyJsonStr{}",bodyJsonStr);
            return;
        }
        Long userID=unCollectNoteMQDTO.getUserId();
        Long noteID=unCollectNoteMQDTO.getNoteId();
        LocalDateTime createTime=LocalDateTime.now();
        Integer type=unCollectNoteMQDTO.getType();
        NoteCollection collection=NoteCollection.builder()
                .userId(userID)
                .noteId(noteID)
                .createTime(createTime)
                .status(type)
                .build();
        //取消收藏
        int count=noteCollectionMapper.update2UnCollectByUserIdAndNoteId(collection);
        if(count==0)return;
        //发送计数MQ
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(bodyJsonStr).build();
        rocketMQTemplate.asyncSend(TOPIC_COUNT_NOTE_COLLECT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数：笔记取消收藏】MQ 发送成功，sendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数：笔记取消收藏】MQ 发送失败，throwable:",throwable);
            }
        });
    }
}
