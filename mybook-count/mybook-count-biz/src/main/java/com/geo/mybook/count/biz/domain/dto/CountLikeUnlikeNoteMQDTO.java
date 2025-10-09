package com.geo.mybook.count.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：14:52
*/
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountLikeUnlikeNoteMQDTO {
    private Long userId;
    private Long noteId;
    private Integer type;
    private Long creatorId;
    private LocalDateTime createTime;
}
