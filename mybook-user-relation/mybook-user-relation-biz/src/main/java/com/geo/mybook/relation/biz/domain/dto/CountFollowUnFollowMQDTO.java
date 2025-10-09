package com.geo.mybook.relation.biz.domain.dto;


/*
creator：AZERL7
createTime：10:29
*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountFollowUnFollowMQDTO {
    private Long userId;

    private Long targetUserId;
    //使用同一个实体类来区别动作类型，1、表示关注，0、表示取关
    private Integer type;
}
