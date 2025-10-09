package com.geo.mybook.relation.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：10:32
*/
@Getter
@AllArgsConstructor
public enum FollowUnFollowTypeEnum {

    UNFOLLOW(0),
    FOLLOW(1),
    ;

    private final Integer code;
}
