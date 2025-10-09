package com.geo.mybook.note.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.note.biz.domain.po.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：9:34
*/
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    Note selectByPrimaryKey(Long id);

    Note existsById(Long noteId);


    /**
     * 用于验证笔记是否存在
     * @param noteId noteId
     * @return int 计数
     */
    int selectCountByNoteId(Long noteId);

    /**
     * 根据笔记id查询作者
     * @param noteId noteId
     * @return Long 用户id
     */
    Long selectCreatorIdByNoteId(Long noteId);

    /**
     * 根据频道id和页数查询需要的note
     * @param topicIds topicIds
     * @param limit pageNo
     * @return noteList
     */
    List<Note> selectByTopicIdsLimit(@Param("topicIds")List<Long> topicIds,@Param("limit") Long limit);
}
