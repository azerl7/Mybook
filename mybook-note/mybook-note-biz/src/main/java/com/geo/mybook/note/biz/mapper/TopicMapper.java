package com.geo.mybook.note.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.note.biz.domain.po.Topic;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
creator：AZERL7
createTime：9:35
*/
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
    /**
     * 根据主键id查询话题
     * @param id 主键id
     * @return string 话题
     */
    String selectNameByPrimaryKey(Long id);

    /**
     * 根据channelId查询话题
     * @param channelId channelId
     * @return topicId_List
     */
    List<Long> selectByChannelId(Long channelId);
}
