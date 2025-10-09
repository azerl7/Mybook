package com.geo.mybook.data.align.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：16:07
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeUnlikeNoteMQDTO {
    private Long userId;
    private Long noteId;
    private Integer type;//0:取消点赞，1：点赞
    private Long creatorId;
    private LocalDateTime createTime;
}
