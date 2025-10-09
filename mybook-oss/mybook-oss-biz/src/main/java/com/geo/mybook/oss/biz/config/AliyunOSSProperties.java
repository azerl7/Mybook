package com.geo.mybook.oss.biz.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
creator：AZERL7
createTime：17:51
*/
@Data
@Component
@ConfigurationProperties(prefix="storage.aliyun-oss")
public class AliyunOSSProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
}
