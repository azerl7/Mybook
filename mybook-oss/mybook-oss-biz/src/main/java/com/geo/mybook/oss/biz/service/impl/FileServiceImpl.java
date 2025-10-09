package com.geo.mybook.oss.biz.service.impl;


import com.geo.framework.common.response.Response;
import com.geo.mybook.oss.biz.service.FileService;
import com.geo.mybook.oss.biz.strategy.FileStrategy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static com.geo.framework.common.util.Constants.MINIO_BUCKET_NAME;

/*
creator：AZERL7
createTime：16:19
*/
@Service
public class FileServiceImpl implements FileService {

    private final FileStrategy fileStrategy;
    public FileServiceImpl(FileStrategy fileStrategy) {
        this.fileStrategy = fileStrategy;
    }

    @Override
    public Response<String> uploadFile(MultipartFile file) {
        //使用用户id作为dir，方便管理
//        String dir= LoginUserContextHolder.getUserId().toString();
//        System.out.println(dir);
//        if(StringUtils.isNotBlank(dir)&&StringUtils.isNotEmpty(dir)){//如果有
//占时留白，等有时间了回来处理，方便后面的删除和覆盖
//        }
//        fileStrategy.uploadFile(file,ALIYUN_BUCKET_NAME,"");
        return Response.success(fileStrategy.uploadFile(file,MINIO_BUCKET_NAME));
    }
}
