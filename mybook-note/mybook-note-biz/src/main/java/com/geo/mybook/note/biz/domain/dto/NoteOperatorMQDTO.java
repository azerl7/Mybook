package com.geo.mybook.note.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：15:17
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteOperatorMQDTO {
    private Long creatorId;
    private Long noteId;
    private Integer type;//0,删除，1，笔记发布
}
