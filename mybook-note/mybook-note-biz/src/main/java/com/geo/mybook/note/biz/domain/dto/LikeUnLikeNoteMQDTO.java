package com.geo.mybook.note.biz.domain.dto;


/*
creator：AZERL7
createTime：11:12
*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeUnLikeNoteMQDTO {
    private Long userId;
    private Long noteId;
    //0:取消点赞 ，1：点赞
    private Integer type;
    private LocalDateTime createTime;
    private Long creatorId;//作者id
}
