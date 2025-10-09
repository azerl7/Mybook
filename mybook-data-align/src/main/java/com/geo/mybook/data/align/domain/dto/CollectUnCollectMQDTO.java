package com.geo.mybook.data.align.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：16:52
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectUnCollectMQDTO {
    private Long noteId;
    private Long userId;
    private Integer type;//0取消收藏，1收藏
    private LocalDateTime createTime;
    private Long creatorId;
}
