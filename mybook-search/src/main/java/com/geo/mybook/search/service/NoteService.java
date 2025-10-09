package com.geo.mybook.search.service;


import com.geo.framework.common.response.PageResponse;
import com.geo.mybook.search.domain.vo.SearchNoteReqVO;
import com.geo.mybook.search.domain.vo.SearchNoteResVO;

/*
creator：AZERL7
createTime：16:31
*/
public interface NoteService {
    /**
     * 搜索笔记
     * @param searchNoteReqVO 搜索笔记请求
     * @return pageReponse
     */
    PageResponse<SearchNoteResVO> searchNote(SearchNoteReqVO searchNoteReqVO);
}
