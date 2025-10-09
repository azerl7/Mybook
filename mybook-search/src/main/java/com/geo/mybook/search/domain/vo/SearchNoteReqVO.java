package com.geo.mybook.search.domain.vo;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
creator：AZERL7
createTime：16:27
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchNoteReqVO {
    @NotBlank(message="搜索内容不能为空")
    private String keyword;
    @Min(value=1,message = "页码已经到开头了")
    private Integer pageNo=1;
    private Integer type;//0：图文，1、视频
    private Integer sort;//排序：null:不限，1、最多点赞，2、最多评论，3、最多收藏
    //发布时间范围：null：不限 / 0：一天内 / 1：一周内 / 2：半年内
    private Integer publishTimeRange;
}
