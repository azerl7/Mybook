package com.geo.mybook.kv.biz;


import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.kv.biz.domain.po.NoteContent;
import com.geo.mybook.kv.biz.domain.repository.NoteContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

/*
creator：AZERL7
createTime：12:30
*/
@Slf4j
@SpringBootTest
public class CassandraTest {

    @Autowired
    private NoteContentRepository noteContentRepository;

    @Test
    void testInsert(){
        NoteContent noteContent=NoteContent.builder()
                .id(UUID.randomUUID())
                .content("Spring 整合 Cassandra")
                .build();
        noteContentRepository.save(noteContent);
    }

    @Test
    void testUpdate(){
        NoteContent noteContent=NoteContent.builder()
                .id(UUID.fromString("f3c112dc-420b-400e-b47d-d16fb363625a"))
                .content("spring 整合 cassandra update")
                .build();
        noteContentRepository.save(noteContent);
    }

    @Test
    void testSelect(){
        Optional<NoteContent> optional=noteContentRepository.findById(UUID.fromString("f3c112dc-420b-400e-b47d-d16fb363625a"));
        optional.ifPresent(noteContent -> log.info("查询结果：{}", JsonUtils.toJsonString(noteContent)));
        //整体感觉有点慢啊
    }

    @Test
    void testDelete(){
        noteContentRepository.deleteById(UUID.fromString("f3c112dc-420b-400e-b47d-d16fb363625a"));
    }
}
