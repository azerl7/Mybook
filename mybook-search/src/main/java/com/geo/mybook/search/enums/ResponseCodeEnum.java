package com.geo.mybook.search.enums;


import com.geo.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：15:43
*/
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("SEARCH-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("SEARCH-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}