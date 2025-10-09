package com.geo.mybook.auth.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：11:57
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailLoginResVO {
    private Long userId;
    private String token;
}
