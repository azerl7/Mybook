package com.geo.mybook.auth.domain.vo;


import com.geo.framework.common.vaildator.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/*
creator：AZERL7
createTime：10:37
*/

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationVo implements VerificationVo {
    @NotBlank(message="邮件不能为空")
    @Email(message="邮箱格式不正确")
    private String email;

    @Override
    public String getData() {
        return email;
    }
}
