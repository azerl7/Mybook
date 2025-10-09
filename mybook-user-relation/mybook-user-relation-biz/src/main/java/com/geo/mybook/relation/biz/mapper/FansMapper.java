package com.geo.mybook.relation.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.relation.biz.domain.po.Fans;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：14:33
*/
@Mapper
public interface FansMapper extends BaseMapper<Fans> {

    /**
     * 统计用户粉丝数量
     * @param userId  用户id
     * @return long
     */
    long selectCountByUserId(Long userId);


    /**
     * 分页查询粉丝
     * @param userId 用户id
     * @param offset 偏移量
     * @param limit 分页大小
     * @return list
     */
    List<Fans> selectPageListByUserId(@Param("userId")Long userId,
                                      @Param("offset")Long offset,
                                      @Param("limit")Long limit);

    /**
     * 查询最新关注的5000个粉丝
     * @param userId 用户id
     * @return list
     */
    List<Fans> select5000FansByUserId(Long userId);
}