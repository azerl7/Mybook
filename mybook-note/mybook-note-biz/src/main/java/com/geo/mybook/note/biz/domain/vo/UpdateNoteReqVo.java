package com.geo.mybook.note.biz.domain.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
creator：AZERL7
createTime：10:55
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateNoteReqVo {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;

    @NotNull(message = "笔记类型不能为空")
    private Integer type;

    private List<String> imgUrls;

    private String videoUrl;

    private String title;

    private String content;

    private Long topicId;
}