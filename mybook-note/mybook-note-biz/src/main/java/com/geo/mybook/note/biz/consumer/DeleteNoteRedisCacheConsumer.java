package com.geo.mybook.note.biz.consumer;


import com.geo.mybook.note.biz.constant.MQConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.geo.framework.common.util.Constants.NOTE_DETAIL_KEY;
import static com.geo.mybook.note.biz.constant.MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE;

/*
creator：AZERL7
createTime：11:18
*/
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, // Group
        topic = TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE)// 消费的主题 Topic
// just one is ok
public class DeleteNoteRedisCacheConsumer implements RocketMQListener<String> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(String s) {
        Long noteId=Long.valueOf(s);
        log.info("## delay delete message consume is success,noteId {}",noteId);
        String noteDetailRedisKey=NOTE_DETAIL_KEY+noteId;
        stringRedisTemplate.delete(noteDetailRedisKey);
    }
}
