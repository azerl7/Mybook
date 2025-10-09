package com.geo.mybook.note.biz.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：12:40
*/
@Getter
@AllArgsConstructor
public enum NoteUnlikeLuaResultEnum {
    NOT_EXIST(-1L),
    NOTE_LIKED(1L),
    NOTE_NOTE_LIKED(0L),
    ;
    private final Long code;

    public static NoteUnlikeLuaResultEnum valueOf(Long code){
        for(NoteUnlikeLuaResultEnum en:NoteUnlikeLuaResultEnum.values()){
            if(ObjectUtil.equal(en.getCode(),code)){
                return en;
            }
        }
        return null;
    }
}
