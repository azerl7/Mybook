package com.geo.mybook.relation.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：14:55
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnfollowUserMQDTO {

    private Long userId;
    private Long unfollowUserId;
    private LocalDateTime createTime;
}
