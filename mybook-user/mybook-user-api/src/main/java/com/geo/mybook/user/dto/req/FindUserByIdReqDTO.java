package com.geo.mybook.user.dto.req;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：14:26
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindUserByIdReqDTO {
//    @NotNull(message="用户id不能为空")
    private Long userId;
}