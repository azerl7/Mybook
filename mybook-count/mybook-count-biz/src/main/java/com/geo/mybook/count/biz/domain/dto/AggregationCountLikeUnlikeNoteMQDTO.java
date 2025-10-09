package com.geo.mybook.count.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：12:00
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregationCountLikeUnlikeNoteMQDTO {
    private Long creatorId;
    private Long noteId;
    private Integer count;
}
