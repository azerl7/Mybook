package com.geo.mybook.count.biz.consumer;


/*
creator：AZERL7
createTime：11:31
*/

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.domain.dto.AggregationCountCollectUnCollectNoteMQDTO;
import com.geo.mybook.count.biz.mapper.NoteCountMapper;
import com.geo.mybook.count.biz.mapper.UserCountMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_NOTE_COLLECT_2_DB;

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_NOTE_COLLECT_2_DB,
        topic= TOPIC_COUNT_NOTE_COLLECT_2_DB
)
public class CountNoteCollect2DBConsumer implements RocketMQListener<String> {

    private final RateLimiter rateLimiter = RateLimiter.create(5000);

    @Resource
    private NoteCountMapper noteCountMapper;

    @Resource
    private UserCountMapper userCountMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void onMessage(String s) {
        rateLimiter.acquire();
        log.info("## CountNoteCollect2DBConsumer 消费了消息: {}", s);

        List<AggregationCountCollectUnCollectNoteMQDTO> countList = JSON.parseArray(s, AggregationCountCollectUnCollectNoteMQDTO.class);

        try {
            countList= JsonUtils.parseList(s,AggregationCountCollectUnCollectNoteMQDTO.class);
        } catch (Exception e) {
            log.error("## 解析字符串异常，",e);
            throw new RuntimeException(e);
        }

        if(CollectionUtil.isNotEmpty(countList)){
            countList.forEach(count->{
                Long noteId = count.getNoteId();
                Long creatorId = count.getCreatorId();
                Integer count1=count.getCount();
                //编程式事务控制，
                transactionTemplate.execute(status->{
                    try{
                        noteCountMapper.insertOrUpdateCollectTotalByNoteId(noteId,count1);
                        userCountMapper.insertOrUpdateCollectTotalByUserId(creatorId,count1);
                        return true;
                    }catch(Exception e){
                        status.setRollbackOnly();//设置为回滚
                        log.error("## 【计数服务】数据库操作失败，请检查noteCount和userCount",e);
                    }
                    return false;
                });
            });
        }

    }
}
