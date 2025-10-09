package com.geo.mybook.user.dto.req;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：9:25
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserPasswordReqDTO {
    @NotBlank(message="密码不能为空")
    private String encodePassword;
}
