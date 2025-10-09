package com.geo.mybook.data.align.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：17:20
*/

/**
 * 因为脑子不好使用了，偷懒把 table 和 普通 的写在一起了
 */
@Mapper
public interface DeleteTableMapper {

    /**
     * 删除日增量表：关注数计数变更
     * @param tableNameSuffix 表名后缀
     */
    void deleteDataAlignFollowingCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：粉丝数计数变更
     * @param tableNameSuffix 表名后缀
     */
    void deleteDataAlignFansCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：笔记收藏数计数变更
     * @param tableNameSuffix 表名后缀
     */
    void deleteDataAlignNoteCollectCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：用户被收藏数计数变更
     * @param tableNameSuffix 表名后缀
     */
    void deleteDataAlignUserCollectCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：用户被点赞数计数变更
     * @param tableNameSuffix 表名后缀
     */
    void deleteDataAlignUserLikeCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：笔记点赞数计数变更
     * @param tableNameSuffix 表名后缀
     */
    void deleteDataAlignNoteLikeCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：笔记发布数计数变更
     * @param tableNameSuffix 表名后缀
     */
    void deleteDataAlignNotePublishCountTempTable(String tableNameSuffix);

    /**
     * 日增量表：关注数计数变更 - 批量删除
     * @param userIds 之前查询过的用户ids
     */
    void batchDeleteDataAlignFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("userIds") List<Long> userIds);

    /**
     * 日增量表：笔记点赞计数变更 - 批量删除
     */
    void batchDeleteDataAlignNoteLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                    @Param("noteIds") List<Long> noteIds);
}
