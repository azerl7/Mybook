package com.geo.mybook.note.biz.domain.vo;


/*
creator：AZERL7
createTime：9:34
*/

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetChannelNoteListReqVO {

    @NotNull(message="频道id不能为空哦")
    private Long channelId;

    @Min(value=1,message = "已经到开头啦")
    private Long pageNo;
}
