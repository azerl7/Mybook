package com.geo.mybook.count.biz.consumer;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.count.biz.domain.dto.CountFollowUnfollowMQDTO;
import com.geo.mybook.count.biz.enums.FollowUnfollowTypeEnum;
import com.geo.mybook.count.biz.mapper.UserCountMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.geo.mybook.count.biz.constant.MQConstants.TOPIC_COUNT_FOLLOWING_2_DB;

/*
creator：AZERL7
createTime：17:42
*/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group_"+TOPIC_COUNT_FOLLOWING_2_DB,
        topic=TOPIC_COUNT_FOLLOWING_2_DB
)
public class CountFollowing2DBConsumer implements RocketMQListener<String> {

    @Resource
    private UserCountMapper userCountMapper;

    private final RateLimiter rateLimiter=RateLimiter.create(5000);

    @Override
    public void onMessage(String s) {
        rateLimiter.acquire();
        log.info("## 消费了 MQ 【计数：关注数入库】，{}",s);
        if(StringUtils.isBlank(s))return;
        CountFollowUnfollowMQDTO countFollowUnfollowMQDTO= JsonUtils.parseObject(s,CountFollowUnfollowMQDTO.class);

        if(ObjectUtil.isNull(countFollowUnfollowMQDTO)){
            log.error("## 【计数：关注数入库】获取对象失败，支付串转换对象失败");
            return;
        }
        Integer type=countFollowUnfollowMQDTO.getType();
        Long userId=countFollowUnfollowMQDTO.getUserId();
        int count= ObjectUtil.equal(type, FollowUnfollowTypeEnum.FOLLOW.getCode()) ? 1 : -1;
        userCountMapper.insertOrUpdateFollowingTotalByUserId(userId,count);
    }
}
