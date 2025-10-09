package com.geo.framework.common.util;


import cn.hutool.core.bean.BeanUtil;

import java.util.Map;
import java.util.stream.Collectors;

/*
creator：AZERL7
createTime：16:00
*/
public class SerializedUtils {
    public static Map<String,String> serializedToUseRedisMap(Object object){
        Map<String,Object> map=BeanUtil.beanToMap( object);
        return map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry-> entry.getValue()==null ? " " :entry.getValue().toString()//存入空值防止缓存穿透
        ));
    }
}
