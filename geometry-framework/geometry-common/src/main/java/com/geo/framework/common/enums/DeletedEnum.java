package com.geo.framework.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：17:59
*/
@Getter
@AllArgsConstructor
public enum DeletedEnum {
    YES(true),
    NO(false),
    ;
    private  final Boolean value;
}
