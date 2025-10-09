package com.geo.mybook.oss.biz.service;


import com.geo.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

/*
creator：AZERL7
createTime：16:19
*/
public interface FileService {


    /**
     * 文件上传
     * @param file 需要上传的文件
     * @return response
     */
    Response<String> uploadFile(MultipartFile file);
}
