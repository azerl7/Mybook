package com.geo.mybook.auth.service;


import com.geo.framework.common.response.Response;
import com.geo.mybook.auth.domain.vo.EmailVerificationVo;
import com.geo.mybook.auth.domain.vo.PhoneVerificationVo;

/*
creator：AZERL7
createTime：17:22
*/
public interface VerificationService {

    /**
     * 发送手机验证码
     * @param phoneVerificationVo 手机号验证码传输对象
     * @return response
     */
    Response<?> send(PhoneVerificationVo phoneVerificationVo);

    /**
     * 发送邮箱验证码
     * @param emailVerificationVo 邮箱验证码传输对象
     * @return response
     */
    Response<?> send(EmailVerificationVo emailVerificationVo);
}
