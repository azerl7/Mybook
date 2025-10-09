package com.geo.mybook.note.biz.controller;


import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.framework.common.response.Response;
import com.geo.mybook.note.biz.domain.vo.*;
import com.geo.mybook.note.biz.service.NoteService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.N;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
creator：AZERL7
createTime：11:47
*/
@Slf4j
@RestController
//@RequestMapping("/note")
public class NoteController {

    @Resource
    private NoteService noteService;

    /**
     * publish笔记
     * @param publishNoteReqVO 发布笔记req
     * @return response
     */
    @PostMapping("/publish")
    @ApiOperationLog(description = "发布笔记")
    public Response<?> publishNote(@RequestBody @Validated PublishNoteReqVo publishNoteReqVO){
        return noteService.publishNote(publishNoteReqVO);
    }

    /**
     * 查询笔记详细信息
     * @param findNoteDetailReqVo 查询笔记详细信息
     * @return response
     */
    @PostMapping("/detail")
    @ApiOperationLog(description = "查询笔记详细信息")
    public Response<FindNoteDetailResVo> findNoteDetail(@RequestBody @Validated FindNoteDetailReqVo findNoteDetailReqVo){
        return noteService.findNoteDetail(findNoteDetailReqVo);
    }

    /**
     * 修改笔记细节
     * @param updateNoteReqVo update note req
     * @return response
     */
    @PostMapping("/update")
    @ApiOperationLog(description = "更新笔记详细信息")
    public Response<?> updateNoteDetail(@RequestBody @Validated UpdateNoteReqVo updateNoteReqVo){
        return noteService.updateNote(updateNoteReqVo);
    }

    /**
     * 删除笔记
     * @param deleteNoteReqVo delete note req
     * @return response
     */
    @PostMapping("/delete")
    @ApiOperationLog(description = "delete note")
    public Response<?> deleteNoteDetail(@RequestBody @Validated DeleteNoteReqVo deleteNoteReqVo){
      return noteService.deleteNote(deleteNoteReqVo);
    };

    /**
     * 更改笔记为仅自己可见
     * @param updateNoteVisibleOnlyMeVo 更改笔记可见为仅自己
     * @return response
     */
    @PostMapping("/visible/onlyme")
    @ApiOperationLog(description = "change visible only me")
    public Response<?> updateNoteVisibleOnlyMe(@RequestBody @Validated UpdateNoteVisibleOnlyMeVo updateNoteVisibleOnlyMeVo){
        return noteService.visibleOnly(updateNoteVisibleOnlyMeVo);
    }


    /**
     * 置顶笔记
     * @param topNoteReqVo 置顶笔记req
     * @return response
     */
    @PostMapping("/top")
    @ApiOperationLog(description = "change top status note")
    public Response<?> topNote(@RequestBody @Validated TopNoteReqVo topNoteReqVo){
        return noteService.topNote(topNoteReqVo);
    }


    /**
     * 点赞笔记
     * @param likeNoteReqVo likeNoteReqVo
     * @return response
     */
    @PostMapping("/like")
    @ApiOperationLog(description = "点赞笔记")
    public Response<?> likeNote(@RequestBody @Validated LikeNoteReqVo likeNoteReqVo){
        return noteService.likeNote(likeNoteReqVo);
    }

    /**
     * 取消点赞笔记
     * @param unlikeNoteReqVo unlikeNOteReqVO
     * @return response
     */
    @PostMapping("/unlike")
    @ApiOperationLog(description = "取消点赞笔记")
    public Response<?> unLikeNote(@RequestBody @Validated UnlikeNoteReqVo unlikeNoteReqVo){
        return noteService.unlikeNote(unlikeNoteReqVo);
    }

    /**
     * 收藏笔记
     * @param collectNoteReqVo collectNoteReqVo
     * @return response
     */
    @PostMapping("/collect")
    @ApiOperationLog(description = "收藏笔记")
    public Response<?> collectNote(@RequestBody @Validated CollectNoteReqVo collectNoteReqVo){
        return noteService.collectNote(collectNoteReqVo);
    }

    /**
     * 取消收藏
     * @param unCollectNoteReqVO unCollectNoteReqVo
     * @return response
     */
    @PostMapping("/uncollect")
    @ApiOperationLog(description = "取消收藏笔记")
    public Response<?> unCollectNote(@RequestBody @Validated UnCollectNoteReqVO unCollectNoteReqVO){
        return noteService.unCollectNote(unCollectNoteReqVO);
    }

    /**
     * 获取频道列表
     * @return response
     */
    @PostMapping("/channel/list")
    @ApiOperationLog(description = "获取频道列表")
    public Response<?> getChannelList(){
        return noteService.getChanelList();
    }



    @PostMapping("/discover/note/list")
    @ApiOperationLog(description = "获取频道下的笔记列表")
    public Response<?> getChannelNoteList(@RequestBody @Validated GetChannelNoteListReqVO getChannelNoteListReqVO){
        return noteService.getChannelNoteList(getChannelNoteListReqVO);
    }
}
