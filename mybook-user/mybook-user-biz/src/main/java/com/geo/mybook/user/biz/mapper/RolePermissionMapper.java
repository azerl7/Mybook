package com.geo.mybook.user.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.user.biz.domain.po.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：11:32
*/
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    /**
     * 根据角色id批量查询
     * @return
     */
    List<RolePermission> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}
