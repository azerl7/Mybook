package com.geo.mybook.oss.biz.enums;


import com.geo.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：17:31
*/
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("OSS-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("OSS-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    VERIFICATION_CODE_SEND_FREQUENTLY("OSS-20000", "请求太频繁，请稍后后再试"),
    UPLOAD_FILE_ERROR("OSS-20001","上传文件失败，请稍后重试，如果仍旧出现问题请联系管理员"),
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}
