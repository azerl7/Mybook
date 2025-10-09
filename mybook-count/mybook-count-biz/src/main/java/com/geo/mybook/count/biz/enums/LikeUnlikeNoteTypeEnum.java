package com.geo.mybook.count.biz.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：14:53
*/
@Getter
@AllArgsConstructor
public enum LikeUnlikeNoteTypeEnum {
    //点赞
    LIKE(1),
    //取消点赞
    UNLIKE(0),
    ;

    private final Integer code;

    public static LikeUnlikeNoteTypeEnum valueOf(Integer code){
        for(LikeUnlikeNoteTypeEnum en:LikeUnlikeNoteTypeEnum.values()){
            if(ObjectUtil.equal(en.getCode(),code)){
                return en;
            }
        }
        return null;
    }
}
