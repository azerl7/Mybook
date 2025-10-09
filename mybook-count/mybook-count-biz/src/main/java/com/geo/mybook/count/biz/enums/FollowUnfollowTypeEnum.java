package com.geo.mybook.count.biz.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：11:48
*/
@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {
    UNFOLLOW(0),
    FOLLOW(1),
    ;

    private final Integer code;

    public static FollowUnfollowTypeEnum valueOf(Integer code){
        for(FollowUnfollowTypeEnum thisEnum: FollowUnfollowTypeEnum.values()){
            if(ObjectUtil.equal(code,thisEnum.getCode())){
                return thisEnum;
            }
        }
        return null;
    }
}
