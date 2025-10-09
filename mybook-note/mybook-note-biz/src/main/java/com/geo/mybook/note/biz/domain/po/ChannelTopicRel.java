package com.geo.mybook.note.biz.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelTopicRel {
    private Long id;

    private Long channelId;

    private Long topicId;

    private Date createTime;

    private Date updateTime;
}