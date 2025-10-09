package com.geo.mybook.search.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：15:39
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchUserResVO {
    private Long userId;
    private String mybookId;
    private String nickName;
    private String avatar;
    private Integer noteTotal;
    private String fansTotal;
    private String highlightNickname;
}
