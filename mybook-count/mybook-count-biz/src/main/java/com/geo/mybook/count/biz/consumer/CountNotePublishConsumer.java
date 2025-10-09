package com.geo.mybook.count.biz.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.constant.MQConstants;
import com.geo.mybook.count.biz.domain.dto.NoteOperateMQDTO;
import com.geo.mybook.count.biz.mapper.UserCountMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.geo.framework.common.util.Constants.COUNT_NOTE_KEY_PREFIX;
import static com.geo.framework.common.util.Constants.FIELD_NOTE_TOTAL;
import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_NOTE_OPERATE;

/*
creator：AZERL7
createTime：15:32
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_NOTE_OPERATE,
        topic=TOPIC_NOTE_OPERATE
)
public class CountNotePublishConsumer implements RocketMQListener<Message> {
    private final RateLimiter rateLimiter = RateLimiter.create(5000);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserCountMapper userCountMapper;

    @Override
    public void onMessage(Message message) {
        rateLimiter.acquire();
        String  bodyJsonStr=new String(message.getBody());
        String tags=message.getTags();
        log.info("==> ConsumerNotePublishConsumer 消费了消息 {},tags{}",bodyJsonStr,tags);

        if(Objects.equals(tags, MQConstants.TAG_NOTE_PUBLISH)){
            handleNoteMessage(bodyJsonStr,1);
        }else if(Objects.equals(tags, MQConstants.TAG_NOTE_DELETE)){
            handleNoteMessage(bodyJsonStr,-1);
        }
    }

    /**
     * 处理笔记消息
     * @param bodyJsonStr bodyJsonStr
     */
    private void handleNoteMessage(String bodyJsonStr,Integer count){
        NoteOperateMQDTO noteOperateMQDTO= JsonUtils.parseObject(bodyJsonStr, NoteOperateMQDTO.class);
        if(ObjectUtil.isNull(noteOperateMQDTO)){
            log.error("## json字符串转换失败，请检查消息传递过来的数据");
            return;
        }
        //笔记发布者的id
        Long creatorId=noteOperateMQDTO.getCreatorId();
        String countUserRedisKey=COUNT_NOTE_KEY_PREFIX+creatorId;
        boolean isCountExist=stringRedisTemplate.hasKey(countUserRedisKey);
        //同样是存在在更新，不存在就在需要的服务上进行初始化
        if(isCountExist){
            stringRedisTemplate.opsForHash().increment(countUserRedisKey,FIELD_NOTE_TOTAL,count);
        }
        //更新数据库
        userCountMapper.insertOrUpdateNoteTotalByUserId(creatorId,count);
    }
}
