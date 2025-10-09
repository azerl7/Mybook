package com.geo.mybook.auth.domain.vo;


import com.geo.framework.common.vaildator.Email;
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
public class UserEmailLoginReqVo implements UserLoginReqVo {

    /**
     * 手机号
     */
    @NotBlank(message="邮箱不能为空")
    @Email(message="邮箱格式不正确")
    private String email;

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
