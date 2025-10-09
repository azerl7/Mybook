package com.geo.mybook.count.biz.consumer;


/*
creator：AZERL7
createTime：14:45
*/

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.mapper.UserCountMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_FANS_2_DB;

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_FANS_2_DB,
        topic=TOPIC_COUNT_FANS_2_DB
)
public class CountFans2DBConsumer implements RocketMQListener<String> {

    private final RateLimiter rateLimiter= RateLimiter.create(5000);

    @Resource
    private UserCountMapper userCountMapper;

    @Override
    public void onMessage(String s) {

        rateLimiter.acquire();//令牌桶限制流量
        log.info("## 消费到了 MQ 【计数：粉丝数入库】,{}...",s);

        //解析json字符串
        Map<Long,Integer> countMap=null;
        try{
            countMap= JsonUtils.parseMap(s,Long.class,Integer.class);
        }catch(Exception e){
            log.error("## 解析字符串异常：",e);
        }

        if (CollUtil.isNotEmpty(countMap)) {
            // 判断数据库中，若目标用户的记录不存在，则插入；若记录已存在，则直接更新 on duplicate key
            //todo:更新批量插入
            countMap.forEach((k, v) -> userCountMapper.insertOrUpdateFansTotalByUserId(v, k));
        }

    }
}
