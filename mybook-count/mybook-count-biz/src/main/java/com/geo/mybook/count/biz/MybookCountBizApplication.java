package com.geo.mybook.count.biz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
creator：AZERL7
createTime：19:59
*/
@SpringBootApplication
@MapperScan("com.geo.mybook.count.biz.mapper")
public class MybookCountBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybookCountBizApplication.class,args);
    }
}
