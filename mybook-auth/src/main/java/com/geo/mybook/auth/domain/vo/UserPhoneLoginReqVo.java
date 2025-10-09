package com.geo.mybook.auth.domain.vo;


import com.geo.framework.common.vaildator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：16:44
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPhoneLoginReqVo implements UserLoginReqVo{

    /**
     * 手机号
     */
    @NotBlank(message="手机号不能为空")
    @PhoneNumber(message="手机号格式不正确")
    private String phone;

    /**
     * 验证码
     */
    private String code;//验证码

    /**
     * 密码
     */
    private String password;//密码

    /**
     * 登录类型
     */
    @NotNull(message="登录类型不能为空")
    private Integer type;
}
