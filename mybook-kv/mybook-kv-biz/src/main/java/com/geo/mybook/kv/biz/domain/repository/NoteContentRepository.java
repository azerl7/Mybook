package com.geo.mybook.kv.biz.domain.repository;

import com.geo.mybook.kv.biz.domain.po.NoteContent;
import org.springframework.data.cassandra.repository.CassandraRepository;
import java.util.UUID;

/*
creator：AZERL7
createTime：12:16
*/
public interface NoteContentRepository extends CassandraRepository<NoteContent, UUID> {

}
