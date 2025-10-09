package com.geo.mybook.data.align.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.domain.dto.FollowUnFollowMQDTO;
import com.geo.mybook.data.align.mapper.InsertMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static com.geo.framework.common.util.Constants.BLOOM_TODAY_USER_FANS_LIST_KEY;
import static com.geo.framework.common.util.Constants.BLOOM_TODAY_USER_FOLLOW_LIST_KEY;
import static com.geo.mybook.data.align.constants.MQConstants.TOPIC_COUNT_FOLLOWING;

/*
creator：AZERL7
createTime：19:52
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_FOLLOWING,
        topic=TOPIC_COUNT_FOLLOWING
)
public class TodayUserFollowIncrementData2DBConsumer implements RocketMQListener<String> {

    @Value("${table.shards}")
    private int tableShards;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private InsertMapper InsertMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void onMessage(String s) {
        log.info("## TodayUserFollowIncrementData2DBConsumer 消费了 MQ :{}",s);
        //---源用户信息变更记录---//
        //1、布隆过滤器判断该日增数据是否已经记录
        FollowUnFollowMQDTO followUnFollowMQDTO = JSON.parseObject(s,FollowUnFollowMQDTO.class);
        if(ObjectUtil.isNull(followUnFollowMQDTO)){
            log.error("## json字符串转换失败，请检查消息信息，topic:{}",TOPIC_COUNT_FOLLOWING);
            return ;
        }
        Long userId = followUnFollowMQDTO.getUserId();
        Long targetUserId = followUnFollowMQDTO.getTargetUserId();
        String date= LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String userBloomKey=BLOOM_TODAY_USER_FOLLOW_LIST_KEY+date;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_user_follow_check.lua")));
        script.setResultType(Long.class);
        long result=stringRedisTemplate.execute(script, Collections.singletonList(userBloomKey),userId.toString());
        RedisScript<Long> bloomAddScript =RedisScript.of("return redis.call('BF.ADD',KEYS[1],ARGV[1])",Long.class);
        if(ObjectUtil.equal(result,0L)){
            //2、如果没有则落库减轻数据库压力
            try{
                long userIdHashkey=userId % tableShards;
                InsertMapper.insert2DataAlignUserFollowingCountTempTable(TableConstants.buildTableNameSuffix(date,userIdHashkey),userId);
            }catch(Exception e){
                log.error("",e);
            }
            //3、添加到布隆过滤器
            stringRedisTemplate.execute(bloomAddScript,Collections.singletonList(userBloomKey),userId.toString());
        }

        //---目标用户变更记录---//
        //1、布隆过滤器判断该日增记录是否已经记录
        String targetBloomKey=BLOOM_TODAY_USER_FANS_LIST_KEY+date;
        result= stringRedisTemplate.execute(script, Collections.singletonList(targetBloomKey),targetUserId.toString());
        if(ObjectUtil.equal(result,0L)) {
            //2、如果没有则落库减轻数据库压力（单个数据库操作就没必要进行事务管理了）
            try{
                long targetUserIdHashKey= targetUserId % tableShards;
                InsertMapper.insert2DataAlignUserFansCountTempTable(TableConstants.buildTableNameSuffix(date,targetUserIdHashKey),targetUserId);
            }catch(Exception e){
                log.error("",e);
            }
            //3、添加到布隆过滤器
            stringRedisTemplate.execute(bloomAddScript,Collections.singletonList(targetBloomKey),targetUserId.toString());
        }
    }
}
