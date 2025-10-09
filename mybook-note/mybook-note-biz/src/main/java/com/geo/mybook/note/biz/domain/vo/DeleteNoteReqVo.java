package com.geo.mybook.note.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：9:55
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteNoteReqVo {

    @NotNull(message="note id can't be null")
    private Long id;
}
