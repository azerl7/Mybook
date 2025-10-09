package com.geo.mybook.data.align.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：23:16
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowUnFollowMQDTO{
    private Long userId;
    private Long targetUserId;
    private Integer type;//0取关，1关注
}
