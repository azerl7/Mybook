package com.geo.mybook.relation.biz.config;


import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/*
creator：AZERL7
createTime：22:25
*/
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
