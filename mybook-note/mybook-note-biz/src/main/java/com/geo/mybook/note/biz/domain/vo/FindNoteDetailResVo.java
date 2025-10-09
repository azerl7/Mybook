package com.geo.mybook.note.biz.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/*
creator：AZERL7
createTime：16:31
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindNoteDetailResVo {

    private Long id;

    private Integer type;

    private String title;

    private String content;

    private List<String> imgUris;

    private Long topicId;

    private String topicName;

    private Long creatorId;

    private String creatorName;

    private String avatar;

    private String videoUri;

    private LocalDateTime updateTime;

    private Integer visible;
}
