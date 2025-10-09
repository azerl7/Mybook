package com.geo.mybook.gateway.enums;


import com.geo.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：8:54
*/
@Getter
@AllArgsConstructor
public enum ResponseEnum implements BaseExceptionInterface {
    SYSTEM_ERROR("500","系统繁忙请稍后重试"),
    UNAUTHORIZED("401","权限不足"),
    ;

    private String errorCode;
    private String errorMessage;

}
