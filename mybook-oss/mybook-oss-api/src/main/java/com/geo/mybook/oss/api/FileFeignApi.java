package com.geo.mybook.oss.api;


import com.geo.framework.common.response.Response;
import com.geo.mybook.oss.config.FeignFormConfig;
import com.geo.mybook.oss.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/*
creator：AZERL7
createTime：16:14
*/
@FeignClient(name= ApiConstants.SERVICE_NAME,configuration = FeignFormConfig.class)
public interface FileFeignApi {

    String PREFIX="/file";

    @PostMapping(value=PREFIX+"/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<String> uploadFile(@RequestPart(value="file") MultipartFile file);
}
