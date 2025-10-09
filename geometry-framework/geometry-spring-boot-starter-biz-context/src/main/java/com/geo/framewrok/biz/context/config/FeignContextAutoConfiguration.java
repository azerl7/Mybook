package com.geo.framewrok.biz.context.config;


import com.geo.framewrok.biz.context.interceptor.FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
creator：AZERL7
createTime：17:11
*/
@Configuration
public class FeignContextAutoConfiguration {
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor(){
        return new FeignRequestInterceptor();
    }
}
