package com.geo.mybook.gateway.auth;


import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geo.framework.common.util.JsonUtils;
import io.netty.util.internal.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.geo.framework.common.util.Constants.ROLE_PERMISSIONS_KEY;
import static com.geo.framework.common.util.Constants.USER_ROLES_KEY;

/*
creator：AZERL7
createTime：16:29
*/
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @SneakyThrows
    public List<String> getPermissionList(Object loginId, String loginType) {
        //返回此 loginId 拥有的权限列表
        //todo 从redis获取
        log.info("## 获取用户权限列表 loginId：{}",loginId);
        List<String> userRoleKeys = getRoleList(loginId, loginType);
        if(CollectionUtil.isNotEmpty(userRoleKeys)){
            List<String> rolePermissionKeys=userRoleKeys.stream()
                    .map(ROLE_PERMISSIONS_KEY::concat)
                    .toList();
            List<String> rolePermissionValues=stringRedisTemplate.opsForValue().multiGet(rolePermissionKeys);
            if(CollectionUtil.isNotEmpty(rolePermissionValues)){
                List<String> permissions=new ArrayList<>();
                rolePermissionValues.forEach(rolePermissionValue->{
                    try{
                        List<String> rolePermissions =objectMapper.readValue(rolePermissionValue, new TypeReference<>() {});
                        permissions.addAll(rolePermissions);
                    }catch(Exception e){
                        e.printStackTrace();
                        log.error("==> JSON解析错误 {}",e.getMessage());
                    }
                });
                return permissions;
            }
        }

        return List.of();
    }

    @Override
    @SneakyThrows
    public List<String> getRoleList(Object loginId, String loginType) {
        //返回此 loginId 拥有的角色列表
        //todo 从redis获取
        log.info("## 获取用户角色列表 loginId：{}",loginId);

        //通过 redisKey 构建 用户-角色 关系
        String  userRolesKey=USER_ROLES_KEY+loginId.toString();
        String userRolesValue= stringRedisTemplate.opsForValue().get(userRolesKey);
        if(StringUtil.isNullOrEmpty(userRolesValue)){
            return null;
        }
//        return CollectionUtil.toList(userRolesValue);
        //为什么要使用这个，真的是服了
//        System.out.println(objectMapper.readValue(userRolesValue, new TypeReference<>() {}).toString());
        return objectMapper.readValue(userRolesValue, new TypeReference<>() {});
    }
}
