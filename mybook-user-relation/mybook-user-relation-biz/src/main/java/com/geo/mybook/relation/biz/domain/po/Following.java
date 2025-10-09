package com.geo.mybook.relation.biz.domain.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：14:32
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Following {
    private Long id;
    private Long userId;
    private Long followingUserId;
    private LocalDateTime createTime;
}
