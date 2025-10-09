package com.geo.framework.common.util;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;

import java.util.Date;
import java.util.Map;

///////////////////////////////////
/*
creator：AZERL7
createTime：16:27
*/
public class JwtUtils {
    public static String build(Object data, String key, Long ttlMillis) {
        // 构建JWT
        return JWT.create()
                .addPayloads(BeanUtil.beanToMap(data))          // 添加数据
                .setExpiresAt(new Date(System.currentTimeMillis()+ttlMillis))      // 设置过期时间
                .setKey(key.getBytes())           // 设置签名器
                .sign();                       // 签名生成令牌
    }

    public static Boolean verify(String jwt, String key){
        try{
            JWT jwt1=JWT.of(jwt).setKey(key.getBytes());
            //验证签名过期时间
            JWTValidator.of(jwt1).validateDate();
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public static<R> R parse(String jwt,String key,Class<R> type) throws InstantiationException, IllegalAccessException {
        if(!verify(jwt,key)){
            System.out.println("invalid jwt");
            return null;
        }
        Map<String,Object> payload=JWT.of(jwt)
                .setKey(key.getBytes())
                .getPayloads();
        return BeanUtil.mapToBean(payload,type,true);
    }
}