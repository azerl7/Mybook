package com.geo.framewrok.biz.context.interceptor;


import cn.hutool.core.util.ObjectUtil;
import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import static com.geo.framework.common.util.Constants.USER_ID;

/*
creator：AZERL7
createTime：17:07
*/
@Slf4j
public class FeignRequestInterceptor  implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId= LoginUserContextHolder.getUserId();
        if(ObjectUtil.isNull(userId)){
            log.warn("#### 未获取到用户id，请检查filter是否插入用户id");
        }
        if(ObjectUtil.isNotNull(userId)){
            requestTemplate.header(USER_ID,String.valueOf(userId));
            log.info("#### 服务间拦截器设置id {}",userId);
        }
    }
}
