package com.geo.mybook.note.biz.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：9:12
*/
@Getter
@AllArgsConstructor
public enum NoteCollectLuaResultEnum {
    NOT_EXIST(-1L),
    NOTE_COLLECTED(1L),
    NOTE_COLLECTED_SUCCESS(0L),
    ;
    private final Long code;

    public static NoteCollectLuaResultEnum valueOf(Long code){
        for(NoteCollectLuaResultEnum en:NoteCollectLuaResultEnum.values()){
            if(ObjectUtil.equal(en.getCode(),code)){
                return en;
            }
        }
        return null;
    }
}
