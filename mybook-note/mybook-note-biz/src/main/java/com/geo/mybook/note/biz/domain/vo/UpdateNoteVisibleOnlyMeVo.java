package com.geo.mybook.note.biz.domain.vo;


/*
creator：AZERL7
createTime：9:12
*/

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNoteVisibleOnlyMeVo {
    @NotNull(message="笔记ID 不能为空")
    private Long id;
}