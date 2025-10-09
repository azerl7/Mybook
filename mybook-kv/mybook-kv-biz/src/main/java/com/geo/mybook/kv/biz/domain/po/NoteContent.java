package com.geo.mybook.kv.biz.domain.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

/*
creator：AZERL7
createTime：12:15
*/
@Data
@Builder
@Table("note_content")
@AllArgsConstructor
@NoArgsConstructor
public class NoteContent {
    @PrimaryKey("id")
    private UUID id;
    private String content;
}
