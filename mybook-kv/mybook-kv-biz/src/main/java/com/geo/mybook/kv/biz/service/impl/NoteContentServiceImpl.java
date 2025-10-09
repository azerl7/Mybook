package com.geo.mybook.kv.biz.service.impl;


import com.geo.framework.common.exception.BizException;
import com.geo.framework.common.response.Response;
import com.geo.mybook.kv.biz.domain.po.NoteContent;
import com.geo.mybook.kv.biz.domain.repository.NoteContentRepository;
import com.geo.mybook.kv.biz.enums.ResponseCodeEnum;
import com.geo.mybook.kv.biz.service.NoteContentService;
import com.geo.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.geo.mybook.kv.dto.res.FindNoteContentResDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

/*
creator：AZERL7
createTime：14:17
*/
@Service
public class NoteContentServiceImpl implements NoteContentService    {

    @Resource
    private NoteContentRepository noteContentRepository;

    @Override
    public Response<String> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {
        String uuid=addNoteContentReqDTO.getUuid();
        String content= addNoteContentReqDTO.getContent();
        NoteContent noteContent=NoteContent.builder()
                .id(UUID.fromString(uuid))
                .content(content)
                .build();

        noteContentRepository.save(noteContent);
        return Response.success(noteContent.getId().toString());
    }

    @Override
    public Response<FindNoteContentResDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO) {
        String uuid= findNoteContentReqDTO.getUuid();
        Optional<NoteContent> optional=noteContentRepository.findById(UUID.fromString(uuid));
        if(optional.isEmpty()){//optional.isEmpty()=!optional.isParent()
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }
        NoteContent noteContent=optional.get();
        FindNoteContentResDTO findNoteContentResDTO=FindNoteContentResDTO.builder()
                .uuid(noteContent.getId())
                .content(noteContent.getContent())
                .build();
        return Response.success(findNoteContentResDTO);
    }

    @Override
    public Response<String> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        String uuid=deleteNoteContentReqDTO.getUuid();
        noteContentRepository.deleteById(UUID.fromString(uuid));
        return Response.success();
    }


}
