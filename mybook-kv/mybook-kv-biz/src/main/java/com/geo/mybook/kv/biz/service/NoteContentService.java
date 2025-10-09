package com.geo.mybook.kv.biz.service;


import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.geo.framework.common.response.Response;
import com.geo.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.geo.mybook.kv.dto.res.FindNoteContentResDTO;

/*
creator：AZERL7
createTime：14:17
*/
public interface NoteContentService {

    /**
     * 添加笔记内容
     * @param addNoteContentReqDTO 添加笔记dto
     * @return response
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO );

    /**
     * 查询笔记内容
     * @param findNoteContentReqDTO 查询笔记dto
     * @return response
     */
    Response<FindNoteContentResDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    /**
     * 删除笔记内容
     * @param deleteNoteContentReqDTO 删除笔记内容
     * @return response
     */
    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}
