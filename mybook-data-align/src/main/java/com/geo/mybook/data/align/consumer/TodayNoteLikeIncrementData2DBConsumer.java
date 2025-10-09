package com.geo.mybook.data.align.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.domain.dto.LikeUnlikeNoteMQDTO;
import com.geo.mybook.data.align.mapper.InsertMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static com.geo.framework.common.util.Constants.*;
import static com.geo.mybook.data.align.constants.MQConstants.TOPIC_COUNT_NOTE_LIKE;

/*
creator：AZERL7
createTime：15:59
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_data_align_"+TOPIC_COUNT_NOTE_LIKE,
topic = TOPIC_COUNT_NOTE_LIKE)
public class TodayNoteLikeIncrementData2DBConsumer implements RocketMQListener<String> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private InsertMapper insertMapper;

    @Value("${table.shards}")
    private int tableShard;

    @Override
    public void onMessage(String s) {
        log.info("## TodayNoteLikeIncrementData2DBConsumer 消费了 MQ: {}",s);
        LikeUnlikeNoteMQDTO likeUnlikeNoteMQDTO = JSON.parseObject(s, LikeUnlikeNoteMQDTO.class);
        if(ObjectUtil.isNull(likeUnlikeNoteMQDTO)) {
            log.error("## json字符串转换对象失败，请检查消息信息，topic：{}",TOPIC_COUNT_NOTE_LIKE);
            return;
        }
        Long noteId = likeUnlikeNoteMQDTO.getNoteId();
        Long creatorId = likeUnlikeNoteMQDTO.getCreatorId();
        String date= LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        //--- 笔记的点赞数变更记录 ---//
        //笔记对应的 bloom key
        String noteBloomKey=BLOOM_TODAY_NOTE_LIKE_NOTE_ID_LIST_KEY+date;

        //1、布隆过滤器判断日增量是否入库
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_tody_note_like_check.lua")));
        script.setResultType(Long.class);
        Long result= stringRedisTemplate.execute(script, Collections.singletonList(noteBloomKey),noteId.toString());
        RedisScript<Long> bloomAddScript =RedisScript.of("return redis.call('BF.ADD',KEYS[1],ARGV[1])",Long.class);

        if(ObjectUtil.equal(result,0L)) {//如果不存在则绝对存在
            //2、如果布隆过滤器中没有则入库，减轻数据库压力
            //根据分片总数获取对应的分片号
            long noteIdHashKey=noteId % tableShard;
            //涉及多张表的操作，保证原子性使用编程式事务控制//因为需要单独区分用户和用户的笔记，所以分开了
            try{
                insertMapper.insert2DataAlignNoteLikeCountTempTable(TableConstants.buildTableNameSuffix(date,noteIdHashKey),noteId);
            }catch(Exception e){
                log.error("",e);
            }
            //3、入库完成之后写入布隆过滤器
            stringRedisTemplate.execute(bloomAddScript, Collections.singletonList(noteBloomKey),noteId.toString());
        }

        //--- 笔记发布者获得的点赞变更记录 ---//
        //笔记发布者对应的 bloom key
        String userBloomKey=BLOOM_TODAY_NOTE_LIKE_USER_ID_LIST_KEY+date;
        result=stringRedisTemplate.execute(script,Collections.singletonList(userBloomKey),creatorId.toString());
        if(ObjectUtil.equal(result,0L)) {
            long userIdHashKey=creatorId % tableShard;
            try{
                insertMapper.insert2DataAlignUserLikeCountTempTable(TableConstants.buildTableNameSuffix(date,userIdHashKey),creatorId);
            }catch(Exception e){
                log.error("",e);
            }
            stringRedisTemplate.execute(bloomAddScript, Collections.singletonList(userBloomKey),creatorId.toString());
        }
    }
}