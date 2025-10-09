package com.geo.mybook.auth.controller;


import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.framework.common.response.Response;
import com.geo.mybook.auth.domain.vo.EmailVerificationVo;
import com.geo.mybook.auth.domain.vo.PhoneVerificationVo;
import com.geo.mybook.auth.service.VerificationService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
creator：AZERL7
createTime：17:44
*/
@RestController
@RequestMapping("/verification")
public class VerificationController {

    @Resource
    private VerificationService verificationService;

    /**
     * 向手机号发送验证码
     * @param verification 验证码vo
     * @return response
     */
    @PostMapping("/code/send/phone")
    @ApiOperationLog(description="发送短信验证码")
    public Response<?> send(@RequestBody @Validated PhoneVerificationVo verification){
        return verificationService.send(verification);
    }

    /**
     * 向邮箱发送验证码
     * @param verification 验证码vo
     * @return response
     */
    @PostMapping("/code/send/email")
    @ApiOperationLog(description = "发送邮件验证码")
    public Response<?> send(@RequestBody @Validated EmailVerificationVo verification){
        return verificationService.send(verification);
    }
}
