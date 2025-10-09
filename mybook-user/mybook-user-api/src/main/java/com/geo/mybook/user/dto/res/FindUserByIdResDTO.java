package com.geo.mybook.user.dto.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：14:27
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindUserByIdResDTO {

    private Long id;

    private String nickName;

    private String avatar;

    private String introduction;
}
