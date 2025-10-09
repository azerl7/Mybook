package com.geo.mybook.note.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：10:18
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopNoteReqVo {
    @NotNull(message="笔记id不能为空")
    Long id;

    @NotNull(message="笔记置顶状态不能为空")
    private Boolean isTop;
}
