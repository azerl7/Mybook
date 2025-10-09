package com.geo.mybook.oss.biz.config;


/*
creator：AZERL7
createTime：16:38
*/

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix="storage.minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
}
