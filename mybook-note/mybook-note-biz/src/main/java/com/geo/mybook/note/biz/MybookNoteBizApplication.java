package com.geo.mybook.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/*
creator：AZERL7
createTime：9:15
*/
@SpringBootApplication
@MapperScan("com.geo.mybook.note.biz.mapper")
@EnableFeignClients(basePackages = {"com.geo.mybook.kv.api","com.geo.mybook.distributed.id.generator.api","com.geo.mybook.user.api"})
public class MybookNoteBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybookNoteBizApplication.class,args);
    }
}
