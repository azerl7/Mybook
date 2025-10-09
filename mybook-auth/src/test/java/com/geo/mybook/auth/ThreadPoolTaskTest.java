package com.geo.mybook.auth;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/*
creator：AZERL7
createTime：18:23
*/
@Slf4j
@SpringBootTest
public class ThreadPoolTaskTest {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    void threadPoolTest(){
        threadPoolTaskExecutor.submit(()->{log.info("线程池测试啦");});
    }
}
