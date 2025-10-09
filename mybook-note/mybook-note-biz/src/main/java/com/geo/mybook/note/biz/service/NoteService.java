package com.geo.mybook.note.biz.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.geo.framework.common.response.Response;
import com.geo.mybook.note.biz.domain.po.Note;
import com.geo.mybook.note.biz.domain.vo.*;

/*
creator：AZERL7
createTime：11:08
*/
public interface NoteService extends IService<Note> {

    /**
     * 上传笔记
     * @param publishNoteReqVo 上传笔记所需要的参数类
     * @return response
     */
    public Response<?> publishNote(PublishNoteReqVo publishNoteReqVo);

    /**
     * 获取笔记细节
     * @param findNoteDetailReqVo 获取笔记细节所需要的参数
     * @return response
     */
    public Response<FindNoteDetailResVo> findNoteDetail(FindNoteDetailReqVo findNoteDetailReqVo);
    public Response<?> updateNote(UpdateNoteReqVo updateNoteRqVo);

    /**
     * delete local cache
     * @param noteId note id
     */
    void deleteNoteLocalCache(Long noteId);


    /**
     * delete Note
     * @param deleteNoteReqVo delete note req
     * @return response
     */
    Response<?> deleteNote(DeleteNoteReqVo deleteNoteReqVo);


    /**
     * 设置笔记为仅自己可见
     * @param updateNoteVisibleOnlyMeVo updateNoteVisiblelyMeVo
     * @return response
     */
    Response<?> visibleOnly(UpdateNoteVisibleOnlyMeVo updateNoteVisibleOnlyMeVo);

    /**
     * 设置笔记置顶状态
     * @param topNoteReqVo topNoteReqVo
     * @return response
     */
    Response<?> topNote(TopNoteReqVo topNoteReqVo);


    /**
     * 点赞笔记
     * @param likeNoteReqVo 点赞笔记vo
     * @return response
     */
    Response<?> likeNote(LikeNoteReqVo likeNoteReqVo);

    /**
     *  取消点赞笔记
     * @param unlikeNoteReqVo 取消点赞笔记vo
     * @return response
     */
    Response<?> unlikeNote(UnlikeNoteReqVo unlikeNoteReqVo);

    /**
     * 收藏笔记
     * @param collectNoteReqVo collectNoteReqVo
     * @return response
     */
    Response<?> collectNote(CollectNoteReqVo collectNoteReqVo);

    /**
     *  取消收藏笔记
     * @param unCollectNoteReqVO unCollectNoteReqVo
     * @return response
     */
    Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO);

    /**
     * 获取频道列表
     * @return response
     */
    Response<?> getChanelList();

    /**
     * 根据频道id获取对应频道的笔记列表
     * @param getChannelNoteListReqVO 频道note
     * @return response
     */
    Response<?> getChannelNoteList(GetChannelNoteListReqVO getChannelNoteListReqVO);
}
