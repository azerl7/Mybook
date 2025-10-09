package com.geo.mybook.search.domain.vo;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class SearchUserReqVO {
    @NotBlank(message="搜索内容不能为空")
    private String keyword;
    @Min(value=1,message = "页码已经到开头了")
    private Integer pageNo=1;
}
