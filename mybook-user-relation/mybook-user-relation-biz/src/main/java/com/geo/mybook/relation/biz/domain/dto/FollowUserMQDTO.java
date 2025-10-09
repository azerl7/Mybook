package com.geo.mybook.relation.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：22:26
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowUserMQDTO {
    private Long userId;
    private Long followUserId;
    private LocalDateTime createTime;
}
