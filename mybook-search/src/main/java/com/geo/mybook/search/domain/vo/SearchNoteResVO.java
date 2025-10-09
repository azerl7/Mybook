package com.geo.mybook.search.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：16:27
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchNoteResVO {
    private Long noteId;
    private String cover;
    private String title;
    private String highlightTitle;
    private String avatar;
    private String nickname;
    private String updateTime;
    private String likeTotal;
    private String commentTotal;
    private String collectTotal;
}
