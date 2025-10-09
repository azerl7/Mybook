package com.geo.mybook.user.biz.rpc;


import com.geo.framework.common.response.Response;
import com.geo.mybook.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/*
creator：AZERL7
createTime：16:38
*/
@Component
public class OssRpcService {

    @Resource
    private FileFeignApi fileFeignApi;

    public String uploadFile(MultipartFile file){
        Response<String> response=fileFeignApi.uploadFile(file);
//        System.out.println(response.getData());
        if(!response.isSuccess()){
            return null;
        }
        return response.getData();
    }
}
