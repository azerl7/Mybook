package com.geo.mybook.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.geo.mybook.auth.mapper")
@EnableFeignClients(basePackages = "com.geo.mybook.user.api")
public class MybookAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybookAuthApplication.class, args);
    }

}
