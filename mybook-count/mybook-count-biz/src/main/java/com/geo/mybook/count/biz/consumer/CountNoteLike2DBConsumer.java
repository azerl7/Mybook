package com.geo.mybook.count.biz.consumer;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.domain.dto.AggregationCountLikeUnlikeNoteMQDTO;
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

import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_NOTE_LIKE_2_DB;

/*
creator：AZERL7
createTime：15:19
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup ="mybook_group_"+TOPIC_COUNT_NOTE_LIKE_2_DB,
topic=TOPIC_COUNT_NOTE_LIKE_2_DB)
public class CountNoteLike2DBConsumer implements RocketMQListener<String> {
    @Resource
    private NoteCountMapper  noteCountMapper;
    private final RateLimiter rateLimiter=RateLimiter.create(5000);
    @Resource
    private UserCountMapper userCountMapper;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void onMessage(String s) {
        rateLimiter.acquire();
        log.info("## 消费到了 MQ 【计数：笔记点赞数入库】，{}...",s);
        List<AggregationCountLikeUnlikeNoteMQDTO> countList=null;
        try{
            countList= JsonUtils.parseList(s, AggregationCountLikeUnlikeNoteMQDTO.class);
        }catch(Exception e){
            log.error("## 解析字符串异常",e);
        }
        if(CollectionUtil.isNotEmpty(countList)){
            countList.forEach(item->{
                Long creatorId=item.getCreatorId();
                Long noteId=item.getNoteId();
                Integer count=item.getCount();
                transactionTemplate.execute(status->{
                    try{
                        //需要更新两个数据库
                        noteCountMapper.insertOrUpdateLikeTotalByNoteId(noteId,count);
                        userCountMapper.insertOrUpdateLikeTotalByUserId(creatorId,count);
                    } catch (Exception e) {
                        status.setRollbackOnly();//设置回滚标记
                        throw new RuntimeException(e);
                    }
                    return false;
                });
            });
        }

    }
}
