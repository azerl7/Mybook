package com.geo.mybook.note.biz.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：8:43
*/
@Getter
@AllArgsConstructor
public enum CollectUnCollectEnum {
    COLLECT(1),
    UN_COLLECT(0),
    ;
    private final Integer code;
}
