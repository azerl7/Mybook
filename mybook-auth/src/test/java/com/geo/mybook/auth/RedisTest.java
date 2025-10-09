package com.geo.mybook.auth;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/*
creator：AZERL7
createTime：16:48
*/
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void redisInitTest(){
        stringRedisTemplate.opsForValue().set("username","azerl7");
        stringRedisTemplate.expire("username", Duration.ofMillis(3600000L));
    }

}
