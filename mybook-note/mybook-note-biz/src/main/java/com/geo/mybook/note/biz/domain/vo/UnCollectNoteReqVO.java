package com.geo.mybook.note.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：9:35
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnCollectNoteReqVO {

    @NotNull(message = "笔记id不能为空")
    private Long noteId;
}
