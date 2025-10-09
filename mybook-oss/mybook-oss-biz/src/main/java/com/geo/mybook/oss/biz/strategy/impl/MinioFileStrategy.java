package com.geo.mybook.oss.biz.strategy.impl;

import com.geo.framework.common.exception.BizException;
import com.geo.mybook.oss.biz.config.MinioProperties;
import com.geo.mybook.oss.biz.enums.ResponseCodeEnum;
import com.geo.mybook.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


/*
creator：AZERL7
createTime：16:02
*/
@Slf4j
public class MinioFileStrategy implements FileStrategy {

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private MinioClient minioClient;

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("## 上传文件到 minio...");

        //1、判断文件是否为空
        if(file==null||file.getSize()==0){
            log.error("==> 上传文件异常：文件大小为空");
            throw new RuntimeException("禁止上传空文件");
        }

        //2、获取文件信息
        String originalFileName=file.getOriginalFilename();
        String contentType=file.getContentType();

        //3、生成存储对象名
        String key = UUID.randomUUID().toString().replace("-","");//uuid已经够了腾讯都是uuid
        String suffix=originalFileName.substring(originalFileName.lastIndexOf("."));
        String objectName=String.format("%s%s",key,suffix);
        log.info("==> 开始上传文件到 Minio，ObjectName:{}",objectName);
        try{
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build()
            );
        }catch(Exception e){
            e.printStackTrace();
            throw new BizException(ResponseCodeEnum.UPLOAD_FILE_ERROR);
        }

        String url=String.format("%s/%s/%s",minioProperties.getEndpoint(), bucketName, objectName);
        log.info("==> 文件上传完成 url地址：{}",url);
        return url;
    }
}
