package com.geo.mybook.kv.dto.req;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：14:36
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindNoteContentReqDTO {
    @NotBlank(message="笔记内容uuid不能为空")
    private String uuid;
}
