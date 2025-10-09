package com.geo.framework.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：18:00
*/
@Getter
@AllArgsConstructor
public enum StatusEnum {
    ENABLE((byte)0),
    DISABLE((byte) 1),
    ;
    private final Byte value;
}
