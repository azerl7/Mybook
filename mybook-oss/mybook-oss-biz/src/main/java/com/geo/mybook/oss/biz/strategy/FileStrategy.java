package com.geo.mybook.oss.biz.strategy;


import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/*
creator：AZERL7
createTime：16:00
*/
public interface FileStrategy {

    /**
     * 文件上传
     *
     * @param file       上传的文件 multipartFile
     * @param bucketName 上传到的桶名称
     * @return String 文件路径
     */
    String uploadFile(MultipartFile file, String bucketName);
}
