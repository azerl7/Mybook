package com.geo.mybook.auth.domain.vo;


import com.geo.framework.common.vaildator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/*
creator：AZERL7
createTime：17:13
*/
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVerificationVo implements VerificationVo {
    @NotBlank(message = "手机号不能为空")
    @PhoneNumber(message="手机号格式不正确")
    private String phone;

    @Override
    public String getData() {
        return phone;
    }
}
