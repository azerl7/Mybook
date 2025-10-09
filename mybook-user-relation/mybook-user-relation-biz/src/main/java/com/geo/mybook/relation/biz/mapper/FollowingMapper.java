package com.geo.mybook.relation.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.relation.biz.domain.po.Following;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：14:37
*/
@Mapper
public interface FollowingMapper extends BaseMapper<Following> {

    /**
     * 查询用户关注列表
     * @param userId 用户id
     * @return list --> following
     */
    List<Following> selectByUserId(Long userId);

    /**
     * 查询关注列表的总条数
     * @param userId 被查询的用户id
     * @return long
     */
    long selectCountByUserId(Long userId);


    /**
     * 分页查询关注列表条数
     * @param userId 被查询的用户id
     * @param offset 偏移量
     * @param limit 分页大小
     * @return list
     */
    List<Following> selectPageListByUserId(@Param("userId")Long  userId,
                                           @Param("offset")Long offset,
                                           @Param("limit")Long limit);


    /**
     * 查询用户关注列表 limit 1000
     * @param userId 被查询的用户id
     * @return list
     */
    List<Following> selectLimitByUserId(Long userId);
}
