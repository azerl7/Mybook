package com.geo.mybook.user.biz.enums;


import com.geo.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：11:40
*/
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("USER-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("USER-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    NICK_NAME_VALID_FAIL00("USER-2001","用户昵称不能低于两个字符,也不能超过16个字符,也不能携带特殊字符"),
    SEX_VALID_FAIL("USER-2002","用户性别设置错误"),
    INTRODUCTION_VALID_FAIL("USER-2003","用户简介不能超过100个字符"),
    UPLOAD_AVATAR_FAIL("USER-20002","头像上传失败，请稍后重试"),
    UPLOAD_BACKGROUND_FAIL("USER-20003","背景图片上传失败，请稍后重试"),
    USER_NOT_FOUND("USER-20004","用户不存在"),
    NOT_LOGIN("USER-20005","请登录后重试"),
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}
