package com.geo.mybook.user.biz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*
creator：AZERL7
createTime：11:11
*/
@SpringBootApplication
@MapperScan("com.geo.mybook.user.biz.mapper")
@EnableFeignClients(basePackages = {"com.geo.mybook.oss.api","com.geo.mybook.distributed.id.generator.api"})
public class MybookUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybookUserBizApplication.class,args);
    }
}
