package com.geo.mybook.note.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.note.biz.domain.po.NoteLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：23:16
*/
@Mapper
public interface NoteLikeMapper extends BaseMapper<NoteLike> {
    /**
     * 查询用户是否已经点赞过该笔记了
     * @param userId 用户id
     * @param noteId 笔记id
     * @return int
     */
    int selectCountByUserIdAndNoteId(@Param("userId")Long userId, @Param("noteId")Long noteId);

    /**
     * 用于查询用户点赞过的所有笔记，用来初始化布隆过滤器
     * @param userId 用户id
     * @return list
     */
    List<NoteLike> selectByUserId(@Param("userId") Long userId);

    /**
     * 用于验证用户是否已经点赞该笔记,限制一个
     * @param userId 用户id
     * @param noteId 笔记id
     * @return int
     */
    int selectNoteIsLiked(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 限制访问量
     * @param userId 用户id
     * @param limit 限制访问量
     * @return list
     */
    List<NoteLike> selectLikedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit")  int limit);


    /**
     * 如果数据库中没有该条数据就插入，有就更新
     *
     * @param noteLike noteLike参数
     * @return int
     */
    int insertOrUpdateMybatis(NoteLike noteLike);

    /**
     *  取消点赞
     * @param noteLike noteLike
     * @return int
     */
    int update2UnlikeByUserIdAndNoteId(NoteLike noteLike);
}
