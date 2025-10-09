package com.geo.mybook.relation.biz.domain.vo;


/*
creator：AZERL7
createTime：14:53
*/

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnfollowUserReqVo {

    @NotNull(message="被取关的用户id不能为空")
    private Long unfollowUserId;
}
