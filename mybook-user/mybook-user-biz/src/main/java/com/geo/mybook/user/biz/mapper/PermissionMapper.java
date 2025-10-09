package com.geo.mybook.user.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.user.biz.domain.po.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
creator：AZERL7
createTime：11:36
*/
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 查询所有被启用的权限
     * @return list
     */
    List<Permission> selectAppEnableList();
}
