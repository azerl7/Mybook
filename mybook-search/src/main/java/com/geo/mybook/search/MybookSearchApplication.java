package com.geo.mybook.search;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
creator：AZERL7
createTime：15:20
*/

@EnableScheduling
@SpringBootApplication
public class MybookSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybookSearchApplication.class, args);
    }
}
