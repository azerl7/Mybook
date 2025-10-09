package com.geo.mybook.relation.biz.domain.vo;


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
public class FindFansUserResVo {

    private Long userId;

    private String avatar;

    private String nickname;

    private Long fansTotal;

    private Long noteTotal;

}
