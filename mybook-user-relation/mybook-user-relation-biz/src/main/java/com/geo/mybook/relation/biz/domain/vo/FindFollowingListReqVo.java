package com.geo.mybook.relation.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：11:11
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindFollowingListReqVo {

    @NotNull(message="查询用户id")
    private Long userId;

    @NotNull(message="页码不能为空")
    private Integer pageNo=1;//默认第一页

}
