package com.geo.mybook.kv.api;


import com.geo.framework.common.response.Response;
import com.geo.mybook.kv.constants.ApiConstants;
import com.geo.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.geo.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.geo.mybook.kv.dto.res.FindNoteContentResDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*
creator：AZERL7
createTime：14:27
*/
@FeignClient(name= ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {
    String PREFIX="/kv";

    @PostMapping(value="/note/content/add")
    Response<?> addNoteContent(@RequestBody @Validated AddNoteContentReqDTO addNoteContentReqDTO);

    @PostMapping(value="/note/content/find")
    public Response<FindNoteContentResDTO> findNoteContent(@RequestBody @Validated FindNoteContentReqDTO findNoteContentReqDTO);

    @PostMapping("/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody @Validated DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}
