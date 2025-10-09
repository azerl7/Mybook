package com.geo.mybook.relation.biz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*
creator：AZERL7
createTime：14:21
*/
@SpringBootApplication
@MapperScan("com.geo.mybook.relation.biz.mapper")
@EnableFeignClients(basePackages = {"com.geo.mybook"})
public class MybookUserRelationBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybookUserRelationBizApplication.class,args);
    }
}
