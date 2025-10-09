package com.geo.mybook.note.biz.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：9:59
*/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetChannelNoteListResVO {
    private Integer type;
    private String cover;
    private String videoUri;
    private String title;
    private Long creatorId;
    private String avatar;
    private String nickname;
    private Long likeTotal;
    private Long id;
}
