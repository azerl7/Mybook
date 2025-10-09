package com.geo.mybook.user.dto.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：8:50
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindUserByAccountResDTO {

    private Long id;
    private String password;
}
