package com.geo.mybook.data.align.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.data.align.constants.MQConstants;
import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.domain.dto.NoteOperatorMQDTO;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static com.geo.framework.common.util.Constants.BLOOM_TODAY_USER_NOTE_OPERATOR_LIST_KEY;
import static com.geo.mybook.data.align.constants.MQConstants.TOPIC_NOTE_OPERATE;

/*
creator：AZERL7
createTime：17:36
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_data_align_" + TOPIC_NOTE_OPERATE, // Group 组
        topic = TOPIC_NOTE_OPERATE // 主题 Topic
)
public class TodayNotePublishIncrementData2DBConsumer implements RocketMQListener<String> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${table.shards}")
    private int tableShards;

    @Resource
    private InsertMapper InsertMapper;

    @Override
    public void onMessage(String s) {
        log.info("## TodayNotePublishIncrementData2DBConsumer 消费到了 MQ: {}", s);
        //1、判断布隆过滤器中是否存在
        NoteOperatorMQDTO noteOperatorMQDTO = JsonUtils.parseObject(s, NoteOperatorMQDTO.class);
        if(ObjectUtil.isNull(noteOperatorMQDTO)){
            log.error("## json字符串转换失败，请检查消息信息 topic:{}",TOPIC_NOTE_OPERATE);
            return ;
        }
        Long userId=noteOperatorMQDTO.getCreatorId();
        String date= LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String bloomKey=BLOOM_TODAY_USER_NOTE_OPERATOR_LIST_KEY+date;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_note_publish_check.lua")));
        script.setResultType(Long.class);
        long result= stringRedisTemplate.execute(script, Collections.singletonList(bloomKey),userId.toString());
        if(ObjectUtil.equal(result,0L)){//如果布隆过滤器不存在则落库，减轻数据库压力
            //2、布隆过滤器中不存在则进行入库
            //2.1、根据分片信息进行分表入库
            long userIdHashKey=userId % tableShards;
            InsertMapper.insert2DataAlignUserNotePublishCountTempTable(TableConstants.buildTableNameSuffix(date,userIdHashKey),userId);

            //3、入库之后刷新到布隆过滤器
            RedisScript<Long> redisScript = RedisScript.of("return redis.call('BF.ADD',KEYS[1],ARGV[1])",Long.class);
            stringRedisTemplate.execute(redisScript, Collections.singletonList(bloomKey),userId.toString());
        }
    }
}
