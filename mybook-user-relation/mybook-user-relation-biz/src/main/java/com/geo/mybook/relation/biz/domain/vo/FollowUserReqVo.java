package com.geo.mybook.relation.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：16:04
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowUserReqVo {

    @NotNull(message="被关注用户id不能为空")
    private Long followUserId;
}
