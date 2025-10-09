package com.geo.mybook.oss.biz.controller;


import com.geo.framework.common.response.Response;
import com.geo.mybook.oss.biz.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*
creator：AZERL7
createTime：16:23
*/
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     * @param file 上传的文件
     * @return response
     */
    @PostMapping(value="/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<String> uploadFile(@RequestPart(value="file")MultipartFile file){
        return fileService.uploadFile(file);
    }
}
