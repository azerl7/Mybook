package com.geo.mybook.oss.config;


import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.beans.Encoder;

/*
creator：AZERL7
createTime：16:32
*/
@Configuration
public class FeignFormConfig {

    @Bean
    public SpringFormEncoder feignFormEncoder() {
        return new SpringFormEncoder();
    }
}
