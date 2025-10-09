package com.geo.mybook.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.auth.domain.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/*
creator：AZERL7
createTime：11:05
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
