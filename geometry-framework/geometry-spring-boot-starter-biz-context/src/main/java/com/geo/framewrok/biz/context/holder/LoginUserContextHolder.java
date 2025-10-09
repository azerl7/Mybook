package com.geo.framewrok.biz.context.holder;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

import static com.geo.framework.common.util.Constants.USER_ID;

/*
creator：AZERL7
createTime：10:34
*/
public class LoginUserContextHolder {

    private static final ThreadLocal<Map<String,Object>> LOGIN_USER_CONTEXT_THREAD_LOCAL=
            TransmittableThreadLocal.withInitial(HashMap::new);


    public static void setUserId(Object value){
        LOGIN_USER_CONTEXT_THREAD_LOCAL.get().put(USER_ID,value);
    }

    public static Long getUserId(){
        Object value=LOGIN_USER_CONTEXT_THREAD_LOCAL.get().get(USER_ID);
        if(ObjectUtil.isNull(value)){
            return null;
        }
        return Long.valueOf(value.toString());
    }

    public static void remove(){
        LOGIN_USER_CONTEXT_THREAD_LOCAL.remove();
    }
}