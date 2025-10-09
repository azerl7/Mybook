package com.geo.mybook.count.biz.domain.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：23:07
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCount {
    private Long id;
    private Long userId;
    private Long fansTotal;
    private Long followingTotal;
    private Long noteTotal;
    private Long likeTotal;
    private Long collectTotal;
}
