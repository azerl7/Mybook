package com.geo.mybook.count.biz.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;

/*
creator：AZERL7
createTime：16:48
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteOperateMQDTO {
    private Long creatorId;
    private Long noteId;
    private Integer type;//操作类型0删除1发布
}
