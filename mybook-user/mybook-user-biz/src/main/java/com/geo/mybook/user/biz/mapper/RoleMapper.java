package com.geo.mybook.user.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.user.biz.domain.po.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
creator：AZERL7
createTime：11:26
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询所有被启用的角色
     * @return List<Role>
     */
    List<Role> selectEnabledList();

    /**
     * 根据主键id查询角色
     * @param roleId 角色id
     * @return role
     */
    Role selectByPrimaryKey(Long roleId);
}
