package com.geo.mybook.search.controller;


import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.framework.common.response.PageResponse;
import com.geo.mybook.search.domain.vo.SearchNoteReqVO;
import com.geo.mybook.search.domain.vo.SearchNoteResVO;
import com.geo.mybook.search.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
creator：AZERL7
createTime：16:57
*/
@Slf4j
@RestController
@RequestMapping("/search")
public class NoteSearchController {
    @Resource
    private NoteService noteService;

    /**
     * 搜索笔记
     * @param searchNoteReqVO 搜索笔记请求
     * @return pageResponse
     */
    @PostMapping("/note")
    @ApiOperationLog(description = "搜索笔记")
    public PageResponse<SearchNoteResVO> searchNote(@RequestBody @Validated SearchNoteReqVO searchNoteReqVO) {
        return noteService.searchNote(searchNoteReqVO);
    }


}
