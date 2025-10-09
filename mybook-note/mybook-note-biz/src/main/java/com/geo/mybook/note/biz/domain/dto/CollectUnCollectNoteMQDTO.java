package com.geo.mybook.note.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：8:39
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectUnCollectNoteMQDTO {
    private Long userId;
    private Long noteId;
    private Integer type;//0取消收藏，1、收藏
    private LocalDateTime createTime;
    private Long creatorId;
}
