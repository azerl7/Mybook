package com.geo.mybook.relation.biz.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：11:13
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindFollowingUserResVo {
    private Long userId;

    private String avatar;

    private String nickname;

    private String introduction;
}
