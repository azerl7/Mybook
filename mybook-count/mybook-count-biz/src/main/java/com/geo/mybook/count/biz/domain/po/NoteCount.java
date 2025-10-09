package com.geo.mybook.count.biz.domain.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：23:06
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteCount {
    private Long id;
    private Long noteId;
    private Long likeTotal;
    private Long collectTotal;
    private Long commentTotal;
}
