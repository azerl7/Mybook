package com.geo.mybook.note.biz.rpc;


import com.geo.mybook.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/*
creator：AZERL7
createTime：10:51
*/
@Component
public class DistributedIdGeneratorRpcService {
    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    public String getSnowflakeId(){
        return distributedIdGeneratorFeignApi.getSnowflakeId("mybook-note");//这家伙都没用key非要传
        // 就算浪费网络和性能，你也要保证api的通用性吗，哈基美，你这家伙
    }
}
