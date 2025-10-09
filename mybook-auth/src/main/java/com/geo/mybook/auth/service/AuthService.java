package com.geo.mybook.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.geo.framework.common.response.Response;
import com.geo.mybook.auth.domain.po.User;
import com.geo.mybook.auth.domain.vo.UpdatePasswordReqVo;
import com.geo.mybook.auth.domain.vo.UserEmailLoginReqVo;
import com.geo.mybook.auth.domain.vo.UserPhoneLoginReqVo;

/*
creator：AZERL7
createTime：9:50
*/
public interface AuthService extends IService<User> {

    /**
     * 手机号登录
     * @param userLoginReqVo 手机号传输登录对象
     * @return response
     */
    Response<String> login(UserPhoneLoginReqVo userLoginReqVo);

    /**
     * 邮箱登录
     * @param userLoginReqVo 邮箱传输登录对象
     * @return response
     */
    Response<String> login(UserEmailLoginReqVo userLoginReqVo);

    /**
     * 用户登出
     * @param userId 用户id
     * @return response
     */
    Response<String> logout(Long userId);


    /**
     * 更新用户密码
     * @param updatePasswordReqVo 更新密码传递对象
     * @return response
     */
    Response<String> updatePassword(UpdatePasswordReqVo updatePasswordReqVo);
}
