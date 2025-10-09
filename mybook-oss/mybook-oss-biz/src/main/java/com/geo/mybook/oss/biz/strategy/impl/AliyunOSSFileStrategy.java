package com.geo.mybook.oss.biz.strategy.impl;


import com.aliyun.oss.OSS;
import com.geo.framework.common.exception.BizException;
import com.geo.mybook.oss.biz.config.AliyunOSSProperties;
import com.geo.mybook.oss.biz.enums.ResponseCodeEnum;
import com.geo.mybook.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/*
creator：AZERL7
createTime：16:01
*/

@Slf4j
public class AliyunOSSFileStrategy implements FileStrategy {

    @Resource
    private AliyunOSSProperties aliyunOSSProperties;

    @Resource
    private OSS ossClient;
    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("## 上传文件到 aliyunOSS ...");
        //1、判断文件是否为空
        if(file==null||file.getSize()==0){
            log.error("==> 上传文件异常：文件大小为空");
            throw new RuntimeException("禁止上传空文件");
        }

        //2、获取文件信息
        String originalFileName=file.getOriginalFilename();
        //3、生成存储对象名称
        String key = UUID.randomUUID().toString().replace("-","");//uuid已经够了腾讯都是uuid
        String suffix=originalFileName.substring(originalFileName.lastIndexOf("."));
        String objectName=String.format("%s%s",key,suffix);
        log.info("==> 开始上传文件到 Minio，ObjectName:{}",objectName);

        try{
            ossClient.putObject(bucketName,objectName,new ByteArrayInputStream(file.getInputStream().readAllBytes()));
        }catch(Exception e){
            e.printStackTrace();
            throw new BizException(ResponseCodeEnum.UPLOAD_FILE_ERROR);
        }

        //注意aliyun-oss 路径拼接方式比较怪
        String url=String.format("https://%s.%s/%s", bucketName,aliyunOSSProperties.getEndpoint(), objectName);
        log.info("==> 文件上传完成 url地址：{}",url);
        return url;
    }
}
