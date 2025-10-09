package com.geo.mybook.auth.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotBlank;

/*
creator：AZERL7
createTime：11:01
*/
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordReqVo {

    @NotBlank(message="新密码不能为空")
    private String newPassword;
}
