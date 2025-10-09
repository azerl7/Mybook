package com.geo.mybook.note.biz.domain.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：23:14
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteCollection {
    private Long id;
    private Long userId;
    private Long noteId;
    private LocalDateTime createTime;
    private Integer status;
}
