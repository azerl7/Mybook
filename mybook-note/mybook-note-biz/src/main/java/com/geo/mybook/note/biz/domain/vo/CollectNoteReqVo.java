package com.geo.mybook.note.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：15:41
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectNoteReqVo {
    @NotNull(message="笔记id不为空")
    private Long id;
}
