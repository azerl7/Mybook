package com.geo.mybook.note.biz.rpc;


import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.common.response.Response;
import com.geo.mybook.kv.api.KeyValueFeignApi;
import com.geo.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.geo.mybook.kv.dto.res.FindNoteContentResDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/*
creator：AZERL7
createTime：10:50
*/
@Component
public class KeyValueRpcService {
    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    /**
     * 添加笔记内容
     * @param uuid 笔记uuid
     * @param content 笔记内容
     * @return boolean
     */
    public boolean saveNoteContent(String uuid,String content){
        AddNoteContentReqDTO addNoteContentReqDTO=AddNoteContentReqDTO.builder()
                .uuid(uuid)
                .content(content)
                .build();
        Response<?> response=keyValueFeignApi.addNoteContent(addNoteContentReqDTO);
        return ObjectUtil.isNotNull(response) && response.isSuccess();
    }

    /**
     * 删除笔记内容
     * @param uuid 笔记uuid
     * @return boolean
     */
    public boolean deleteNoteContent(String uuid){
        DeleteNoteContentReqDTO deleteNoteContentReqDTO=DeleteNoteContentReqDTO.builder()
                .uuid(uuid)
                .build();

        Response<?> response=keyValueFeignApi.deleteNoteContent(deleteNoteContentReqDTO);
        return ObjectUtil.isNotNull(response) && response.isSuccess();
    }

    public String findNoteContent(String uuid){
        FindNoteContentReqDTO findNoteContentReqDTO=FindNoteContentReqDTO.builder()
                .uuid(uuid)
                .build();

        Response<FindNoteContentResDTO> response=keyValueFeignApi.findNoteContent(findNoteContentReqDTO);
        if(ObjectUtil.isNull(response)||!response.isSuccess()||ObjectUtil.isNull(response.getData())){
            return null;//getData可能为空的原因是因为没用强制要求存储笔记
        }
        return response.getData().getContent();
    }
}
