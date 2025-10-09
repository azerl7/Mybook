package com.geo.mybook.data.align.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：17:41
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteOperatorMQDTO {
    private Long noteId;
    private Long creatorId;
    private Integer type; //0删除，1、发布
}
