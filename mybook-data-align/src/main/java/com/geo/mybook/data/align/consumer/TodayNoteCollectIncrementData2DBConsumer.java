package com.geo.mybook.data.align.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.data.align.constants.MQConstants;
import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.domain.dto.CollectUnCollectMQDTO;
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
import static com.geo.mybook.data.align.constants.MQConstants.TOPIC_COUNT_NOTE_COLLECT;

/*
creator：AZERL7
createTime：16:48
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_data_align_" +TOPIC_COUNT_NOTE_COLLECT, // Group 组
        topic = TOPIC_COUNT_NOTE_COLLECT // 主题 Topic
)
public class TodayNoteCollectIncrementData2DBConsumer implements RocketMQListener<String> {

    @Resource
    private final StringRedisTemplate stringRedisTemplate;

    @Resource
    private  InsertMapper InsertMapper;

    @Value("${table.shards}")
    private int tableShards;


    public TodayNoteCollectIncrementData2DBConsumer(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void onMessage(String s) {
        log.info("## TodayNoteCollectIncrementData2DBConsumer 消费到了 MQ: {}", s);
        //1、布隆过滤器判断该日增量数据是否被记录
        CollectUnCollectMQDTO collectUnCollectMQDTO= JsonUtils.parseObject(s, CollectUnCollectMQDTO.class);
        if(ObjectUtil.isNull(collectUnCollectMQDTO)){
            log.error("## json 字符串转换失败，请检查消息信息,topic:{}",TOPIC_COUNT_NOTE_COLLECT);
            return;
        }
        Long noteId= collectUnCollectMQDTO.getNoteId();
        Long creatorId= collectUnCollectMQDTO.getCreatorId();
        String date= LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        //--- 笔记数量变更 ---//
        String noteBloomKey=BLOOM_TODAY_NOTE_COLLECT_NOTE_ID_LIST_KEY+date;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_note_collect_check.lua")));
        script.setResultType(Long.class);
        Long result = stringRedisTemplate.execute(script, Collections.singletonList(noteBloomKey), noteId.toString());
        RedisScript<Long> bloomScript =RedisScript.of("return  redis.call('BF.ADD',KEYS[1],ARGV[1])",Long.class);
        if(ObjectUtil.equal(result,0L)){//2、若没有数据则入库
            //2.1、根据id进行分片
            try{
                long noteIdHashKey=noteId % tableShards;
                InsertMapper.insert2DataAlignNoteCollectCountTempTable(TableConstants.buildTableNameSuffix(date,noteIdHashKey),noteId);
            }catch(Exception e){
                log.error("",e);
            }
            //3、存入布隆过滤器
            stringRedisTemplate.execute(bloomScript, Collections.singletonList(noteBloomKey), noteId.toString());
        }

        //--- 被收藏笔记目标用户数变更 ---//
        String userBloomKey=BLOOM_TODAY_NOTE_COLLECT_USER_ID_LIST_KEY+date;
        result=stringRedisTemplate.execute(script, Collections.singletonList(userBloomKey), noteId.toString());
        if(ObjectUtil.equal(result,0L)){//如果没有则进行数据落库
            try{
                long userIdHashkey=creatorId % tableShards;
                InsertMapper.insert2DataAlignUserCollectCountTempTable(TableConstants.buildTableNameSuffix(date,userIdHashkey),creatorId);
            }catch(Exception e){
                log.error("",e);
            }
            //落库完之后进行写入布隆过滤器
            stringRedisTemplate.execute(bloomScript, Collections.singletonList(userBloomKey), creatorId.toString());
        }
    }
}
