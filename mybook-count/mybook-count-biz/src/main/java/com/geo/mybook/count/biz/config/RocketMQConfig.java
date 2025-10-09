package com.geo.mybook.count.biz.config;


/*
creator：AZERL7
createTime：23:05
*/
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
