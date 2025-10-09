package com.geo.mybook.note.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：16:30
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindNoteDetailReqVo {
    @NotNull(message="笔记 ID 不能为空")
    private Long id;
}
