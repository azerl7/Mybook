package com.geo.mybook.count.biz.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/*
creator：AZERL7
createTime：23:07
*/
@Mapper
public interface NoteCountMapper {
    int  insertOrUpdateLikeTotalByNoteId(@Param("noteId")Long noteId,@Param("count")Integer count);
    int insertOrUpdateCollectTotalByNoteId(@Param("noteId")Long noteId, @Param("count")Integer count);
}
