package com.geo.mybook.user.biz.rpc;


import com.geo.mybook.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static com.geo.framework.common.util.Constants.BIZ_TAG_MYBOOK_ID;
import static com.geo.framework.common.util.Constants.BIZ_TAG_USER_ID;

/*
creator：AZERL7
createTime：15:54
*/
@Component
public class DistributedIdGeneratorRpcService {

    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    public String getMybookId(){
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_MYBOOK_ID);
    }

    public String getUserId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }
}
