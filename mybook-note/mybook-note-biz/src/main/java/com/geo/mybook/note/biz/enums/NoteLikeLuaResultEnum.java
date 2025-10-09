package com.geo.mybook.note.biz.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：10:36
*/
@Getter
@AllArgsConstructor
public enum NoteLikeLuaResultEnum {
    //笔记不存在
    NOT_EXIST(-1L),
    //已经点赞过该笔记
    NOTE_LIKED(1L),
    //笔记点赞成功
    NOTE_LIKE_SUCCESS(0L),
    ;

    private final Long code;

    public static NoteLikeLuaResultEnum valueOf(Long code){
        for(NoteLikeLuaResultEnum en:NoteLikeLuaResultEnum.values()){
            if(ObjectUtil.equals(en.getCode(),code)){
                return en;
            }
        }
        return null;
    }
}
