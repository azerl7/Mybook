package com.geo.mybook.count.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：11:46
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountFollowUnfollowMQDTO {

    private Long userId;

    private Long targetUserId;

    private Integer type;
}
