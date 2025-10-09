package com.geo.mybook.note.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.note.biz.domain.po.NoteCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：23:15
*/
@Mapper
public interface NoteCollectionMapper extends BaseMapper<NoteCollection> {
    /**
     * 查询笔记是否被搜藏
     * @param userId  用户id
     * @param noteId  笔记id
     * @return int
     */
    int selectCountByUserIdAndNoteId(@Param("userId")Long userId, @Param("noteId")Long noteId);

    /**
     * 查询用户已收藏的笔记
     * @param userId 用户id
     * @return int
     */
    List<NoteCollection> selectByUserId(Long userId);

    /**
     * 查询笔记是否已经被该用户收藏
     * @param userId 用户id
     * @param noteId 笔记id
     * @return int
     */
    int selectNoteIsCollected(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询用户最近收藏的笔记
     * @param userId 用户id
     * @param limit 查询数量
     * @return list
     */
    List<NoteCollection> selectCollectedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit")  int limit);

    /**
     * 收藏数据落库
     * @param noteCollection noteCollect
     * @return int 受到影响的数据条数
     */
    int insertOrUpdateMybatis(NoteCollection noteCollection);


    /**
     * 取消收藏
     * @param noteCollection noteCollection
     * @return int
     */
    int update2UnCollectByUserIdAndNoteId(NoteCollection noteCollection);
}
