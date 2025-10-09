package com.geo.mybook.kv.dto.req;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：14:13
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddNoteContentReqDTO {
    @NotNull(message="笔记内容uuid不能为空")
    private String uuid;
    @NotBlank(message="笔记内容不能为空")
    private String content;
}
