package com.geo.mybook.note.biz.config;


import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/*
creator：AZERL7
createTime：11:02
*/
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
