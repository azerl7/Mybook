package com.geo.mybook.note.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/*
creator：AZERL7
createTime：11:18
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
}
