package com.geo.mybook.note.biz.consumer;

import com.geo.mybook.note.biz.constant.MQConstants;
import com.geo.mybook.note.biz.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.geo.mybook.note.biz.constant.MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE;

/*
creator：AZERL7
createTime：17:27
*/
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_DELETE_NOTE_LOCAL_CACHE, // Group
        topic = TOPIC_DELETE_NOTE_LOCAL_CACHE, // 消费的主题 Topic
        messageModel = MessageModel.BROADCASTING) // 广播模式
public class DeleteNoteLocalCacheConsumer implements RocketMQListener<String> {

    @Resource
    private NoteService noteService;

    @Override
    public void onMessage(String body) {
        Long noteId = Long.valueOf(body);
        log.info("## 消费者消费成功, noteId: {}", noteId);

        noteService.deleteNoteLocalCache(noteId);
    }
}