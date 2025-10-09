package com.geo.mybook.count.biz.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/*
creator：AZERL7
createTime：23:07
*/
@Mapper
public interface UserCountMapper {
    /**
     * 添加或者更新用户粉丝总数
     * @param count 更新数
     * @param userId 要更新的用户id
     * @return int
     */
    int insertOrUpdateFansTotalByUserId(@Param("count") Integer count, @Param("userId")Long userId);

    /**
     * 添加或者更新用户关注数
     * @param count 关注数
     * @param userId 用户id
     * @return count
     */
    int insertOrUpdateFollowingTotalByUserId(@Param("userId") Long userId ,@Param("count")Integer count);

    /**
     * 添加记录或更新笔记点赞数
     * @param count 计数
     * @param userId 用户id
     * @return count
     */
    int insertOrUpdateLikeTotalByUserId(@Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 添加收藏或者更新收藏笔记数目
     * @param userId 用户id
     * @param count 计数
     * @return count
     */
    int insertOrUpdateCollectTotalByUserId(@Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 添加或者更新发布笔记的数目
     * @param userId 用户id
     * @param count 计数
     * @return count
     */
    int insertOrUpdateNoteTotalByUserId(@Param("userId") Long userId, @Param("count") Integer count);
}
