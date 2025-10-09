package com.geo.mybook.oss.biz.factory;

import org.apache.commons.lang3.StringUtils;
import com.geo.mybook.oss.biz.strategy.FileStrategy;
import com.geo.mybook.oss.biz.strategy.impl.AliyunOSSFileStrategy;
import com.geo.mybook.oss.biz.strategy.impl.MinioFileStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
creator：AZERL7
createTime：16:01
*/
@Configuration
@RefreshScope
public class FileStrategyFactory {
    @Value("${storage.type}")
    private String strategyType;

    @Bean
    @RefreshScope
    public FileStrategy getFileStrategy(){
        if (StringUtils.equals(strategyType, "minio")) {
            return new MinioFileStrategy();
        } else if (StringUtils.equals(strategyType, "aliyun")) {
            return new AliyunOSSFileStrategy();
        }

        throw new IllegalArgumentException("存储类型不可用");
    }
}
