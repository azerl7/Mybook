package com.geo.mybook.user.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.user.biz.domain.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：11:56
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
    Integer updateByIdSelective(User user);

    User selectByAccount(@Param("column")String column,@Param("account") String account);

    User selectById2nickname2avatar2introduction(Long id);

    User selectByIdMybatis(Long id);

    List<User> selectByIds(@Param("ids") List<Long> ids);
}
