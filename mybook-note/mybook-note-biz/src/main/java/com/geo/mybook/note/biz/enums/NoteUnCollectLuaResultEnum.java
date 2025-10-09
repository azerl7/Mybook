package com.geo.mybook.note.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/*
creator：AZERL7
createTime：9:41
*/
@Getter
@AllArgsConstructor
public enum NoteUnCollectLuaResultEnum {
    // 布隆过滤器不存在
    NOT_EXIST(-1L),
    // 笔记已收藏
    NOTE_COLLECTED(1L),
    // 笔记未收藏
    NOTE_NOT_COLLECTED(0L),
    ;

    private final Long code;

    public static NoteUnCollectLuaResultEnum valueOf(Long code) {
        for (NoteUnCollectLuaResultEnum noteUnCollectLuaResultEnum : NoteUnCollectLuaResultEnum.values()) {
            if (Objects.equals(code, noteUnCollectLuaResultEnum.getCode())) {
                return noteUnCollectLuaResultEnum;
            }
        }
        return null;
    }
}
