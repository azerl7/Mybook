package com.geo.mybook.distributed.id.generator.api;


import com.geo.mybook.distributed.id.generator.constants.ApiConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/*
creator：AZERL7
createTime：15:43
*/
@FeignClient(name= ApiConstant.SERVICE_NAME)
public interface DistributedIdGeneratorFeignApi {
    String PREFIX="/id";

    @PostMapping("/segment/get/{key}")
    String getSegmentId(@PathVariable("key") String key);

    @PostMapping("/snowflake/get/{key}")
    String getSnowflakeId(@PathVariable("key") String key);
}
