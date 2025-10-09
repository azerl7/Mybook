package com.geo.mybook.auth.controller;


import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.framework.common.response.Response;
import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import com.geo.mybook.auth.domain.vo.UpdatePasswordReqVo;
import com.geo.mybook.auth.domain.vo.UserEmailLoginReqVo;
import com.geo.mybook.auth.domain.vo.UserPhoneLoginReqVo;
import com.geo.mybook.auth.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/*
creator：AZERL7
createTime：16:29
*/
@Slf4j
@RestController
//@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 手机号登录
     * @param userLoginReqVo 手机号登录vo
     * @return response
     */
    @PostMapping("/login/phone")
    @ApiOperationLog(description = "用户手机登录登录")
    public Response<String> login(@RequestBody @Validated UserPhoneLoginReqVo userLoginReqVo){
        return authService.login(userLoginReqVo);
    }

    /**
     * 邮箱登录
     * @param userLoginReqVo 用户登录vo
     * @return response
     */
    @PostMapping("/login/email")
    @ApiOperationLog(description = "用户邮箱登录")
    public Response<String> login(@RequestBody @Validated UserEmailLoginReqVo userLoginReqVo){
        return authService.login(userLoginReqVo);
    }

    /**
     * 用户登出
     * @return response
     */
    @PostMapping("/logout")
    @ApiOperationLog(description = "用户登出")
    public Response<String> logout(){
        Long userId= LoginUserContextHolder.getUserId();
        return authService.logout(userId);
    }

    /**
     * 更新密码
     * @param updatePasswordReqVO 更新密码的vo
     * @return response
     */
    @PostMapping("/password/update")
    @ApiOperationLog(description = "修改密码")
    public Response<String> updatePassword(@Validated @RequestBody UpdatePasswordReqVo updatePasswordReqVO) {
        return authService.updatePassword(updatePasswordReqVO);
    }
}
