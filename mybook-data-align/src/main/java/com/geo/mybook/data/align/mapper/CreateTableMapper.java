package com.geo.mybook.data.align.mapper;

import org.apache.ibatis.annotations.Mapper;

/*
creator：AZERL7
createTime：15:25
*/
@Mapper
public interface CreateTableMapper {

    /**
     * 创建日增量表：关注数计数变更
     * @param tableNamePrefix 表名
     */
    void createDataAlignFollowingCountTempTable(String tableNamePrefix);

    /**
     * 创建日增量表：粉丝数计数变更
     * @param tableNameSuffix 表名
     */
    void createDataAlignFansCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：笔记收藏数计数变更
     * @param tableNameSuffix 表名
     */
    void createDataAlignNoteCollectCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：用户被收藏数计数变更
     * @param tableNameSuffix 表名
     */
    void createDataAlignUserCollectCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：用户被点赞数计数变更
     * @param tableNameSuffix 表名
     */
    void createDataAlignUserLikeCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：笔记点赞数计数变更
     * @param tableNameSuffix 表名
     */
    void createDataAlignNoteLikeCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：笔记发布数计数变更
     * @param tableNameSuffix 表名
     */
    void createDataAlignNotePublishCountTempTable(String tableNameSuffix);
}
