package com.geo.mybook.kv.dto.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/*
creator：AZERL7
createTime：14:36
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindNoteContentResDTO {
    private UUID uuid;
    private String content;
}
