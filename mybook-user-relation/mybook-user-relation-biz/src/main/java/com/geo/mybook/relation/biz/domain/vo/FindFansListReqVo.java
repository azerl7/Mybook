package com.geo.mybook.relation.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：16:48
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindFansListReqVo {

    @NotNull(message="查询用户 id 不能为空")
    private Long userId;

    @NotNull(message="页码为空")
    private Integer pageNo=1;//默认值1
}
