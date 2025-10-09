package com.geo.mybook.data.align.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：1:21
*/
@Mapper
public interface SelectMapper {
    /**
     * 日增量表：关注数计数变更 - 批量查询
     * @param tableNameSuffix 表名
     * @param batchSize 每次查询的数据条数
     * @return list
     */
    List<Long> selectBatchFromDataAlignFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);

    /**
     * 查询 t_following 关注表，获取关注总数
     * @param userId 用户id
     * @return int
     */
    int selectCountFromFollowingTableByUserId(long userId);


    /**
     * 日增量表：笔记点赞数变更 - 批量查询
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignNoteLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                              @Param("batchSize") int batchSize);

    /**
     * 查询 t_note_like 笔记点赞表，获取点赞总数
     * @param noteId
     * @return
     */
    int selectCountFromNoteLikeTableByUserId(long noteId);
}
