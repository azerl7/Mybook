package com.geo.mybook.note.biz.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geo.mybook.note.biz.domain.po.Channel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
creator：AZERL7
createTime：9:35
*/
@Mapper
public interface ChannelMapper extends BaseMapper<Channel> {

    /**
     * 查询需要显示的频道
     * @param limit 限制数量
     * @return list channelList
     */
    List<Channel> selectAllChannelLimit(Integer limit);

    /**
     * 查询所有的频道 （频道也不会太多，直接全部查询也没问题）
     * @return list chanelList
     */
    List<Channel> selectAllChannel();
}
