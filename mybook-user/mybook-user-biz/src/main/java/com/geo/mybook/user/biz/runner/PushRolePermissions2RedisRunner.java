package com.geo.mybook.user.biz.runner;


import cn.hutool.core.collection.CollectionUtil;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.user.biz.domain.po.Permission;
import com.geo.mybook.user.biz.domain.po.Role;
import com.geo.mybook.user.biz.domain.po.RolePermission;
import com.geo.mybook.user.biz.mapper.PermissionMapper;
import com.geo.mybook.user.biz.mapper.RoleMapper;
import com.geo.mybook.user.biz.mapper.RolePermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.geo.framework.common.util.Constants.PUSH_PERMISSION_FLAG;
import static com.geo.framework.common.util.Constants.ROLE_PERMISSIONS_KEY;

/*
creator：AZERL7
createTime：17:21
*/
@Slf4j
@Component
public class PushRolePermissions2RedisRunner implements ApplicationRunner {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {//springboot启动后执行
        log.info("==> 服务启动，同步用户角色权限到redis");
        //todo
        try{
            Boolean canPushed = stringRedisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.DAYS);
            //如果已经存在了，则跳出
            if(Boolean.FALSE.equals(canPushed)){
                log.warn("==> 服务启动，用户角色权限已经同步到redis，无需再次同步");
                return;
            }
            //1、查询出所有角色
            List<Role> roles=roleMapper.selectEnabledList();
            if(CollectionUtil.isNotEmpty(roles)){
                //2、拿到id
                List<Long> roleIds=roles.stream().map(Role::getId).toList();
                //3、根据角色id，批量查询出所有角色对应的权限
                List<RolePermission> rolePermissions=rolePermissionMapper.selectByRoleIds(roleIds);
                //4、按照角色分组,每个角色有多个权限
                Map<Long,List<Long>> roleIdPermissionIdsMap=rolePermissions.stream().collect(
                        Collectors.groupingBy(RolePermission::getRoleId,
                                Collectors.mapping(RolePermission::getPermissionId,Collectors.toList()))
                );
                //5、查询所有被启用的权限，并按照id对应权限的关系进行映射
                List<Permission> permissions=permissionMapper.selectAppEnableList();
                Map<Long,Permission> permissionIdMap=permissions.stream().collect(
                        Collectors.toMap(Permission::getId, permission->permission)
                );

                //6、关联角色和权限的关系
                Map<String,List<String>> roleIdPermissionMap= new HashMap<>();
                roles.forEach(role->{
                    Long roleId=role.getId();
                    String roleKey=role.getRoleKey();
                    List<Long> permissionIds=roleIdPermissionIdsMap.get(roleId);
                    if(CollectionUtil.isNotEmpty(permissionIds)){
                        List<String> pers=new ArrayList<>();
                        permissionIds.forEach(permissionId->{
                            Permission permission=permissionIdMap.get(permissionId);
                            if (permission!=null){
                                pers.add(permission.getPermissionKey());
                            }
                        });
                        roleIdPermissionMap.put(roleKey,pers);
                    }
                });
                //同步至redis
                roleIdPermissionMap.forEach((roleId,permission)->{
                    String key=ROLE_PERMISSIONS_KEY+roleId;
                    stringRedisTemplate.opsForValue().set(key, JsonUtils.toJsonString(permission));
                });
                //优化redis，减少多次网络通信请求，尽量一次请求就完成
                // 使用Redis管道批量写入，减少网络交互
//                stringRedisTemplate.executePipelined(
//                        (RedisCallback<?>) connection -> {
//                            roleIdPermissionMap.forEach((roleId, permission) -> {
//                                String key = ROLE_PERMISSIONS_KEY + roleId;
//                                connection.set(key.getBytes(), JsonUtils.toJsonString(permissions).getBytes());
//                            });
//                            return null;
//                        });
                // 在调用 putAll 之前，将 roleIdPermissionMap 的 key 转换为 String
//                Map<String, Object> stringKeyMap = new HashMap<>();
//                roleIdPermissionMap.forEach((roleId, permission) -> {
//                    stringKeyMap.put(String.valueOf(roleId), String.valueOf(permission));
//                });
//                stringRedisTemplate.opsForHash().putAll(ROLE_PERMISSIONS_KEY, stringKeyMap);
            }
            log.info("==> 服务启动，成功同步用户角色权限到redis");
        }
        catch(Exception e){
            e.printStackTrace();
            log.info("==> 服务启动，同步用户角色权限到redis失败");
        }
    }
}

