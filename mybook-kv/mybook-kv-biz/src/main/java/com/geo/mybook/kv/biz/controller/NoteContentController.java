package com.geo.mybook.kv.biz.controller;


import com.geo.framework.common.response.Response;
import com.geo.mybook.kv.biz.service.NoteContentService;
import com.geo.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.geo.mybook.kv.dto.res.FindNoteContentResDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
creator：AZERL7
createTime：14:22
*/
@Slf4j
@RestController
//@RequestMapping("/kv")
public class NoteContentController {
    @Resource
    private NoteContentService noteContentService;

    /**
     * 添加笔记
     * @param addNoteContentReqDTO 添加笔记dto
     * @return response
     */
    @PostMapping(value="/note/content/add")
    public Response<?> addNoteContent(@RequestBody @Validated AddNoteContentReqDTO addNoteContentReqDTO){
        return noteContentService.addNoteContent(addNoteContentReqDTO);
    }

    /**
     * 查询笔记内容
     * @param findNoteContentReqDTO 查询笔记内容dto
     * @return response
     */
    @PostMapping(value="/note/content/find")
    public Response<FindNoteContentResDTO> findNoteContent(@RequestBody @Validated FindNoteContentReqDTO findNoteContentReqDTO){
        return noteContentService.findNoteContent(findNoteContentReqDTO);
    }

    /**
     * 删除笔记内容
     * @param deleteNoteContentReqDTO 删除笔记dot
     * @return response
     */
    @PostMapping(value="/note/content/delete")
    public Response<?> deleteNoteContent(@RequestBody @Validated DeleteNoteContentReqDTO deleteNoteContentReqDTO){
        return noteContentService.deleteNoteContent(deleteNoteContentReqDTO);
    }
}
