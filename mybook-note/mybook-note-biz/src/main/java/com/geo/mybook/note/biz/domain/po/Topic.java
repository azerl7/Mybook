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
public class Topic {
    private Long id;

    private String name;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;
}