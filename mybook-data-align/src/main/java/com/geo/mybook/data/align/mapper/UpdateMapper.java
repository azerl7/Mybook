package com.geo.mybook.data.align.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
creator：AZERL7
createTime：1:35
*/
@Mapper
public interface UpdateMapper {
    /**
     * 更新 t_user_count 计数表总关注数
     * @param userId 用户id
     * @return count 操作数
     */
    int updateUserFollowingTotalByUserId(@Param("userId") long userId,
                                         @Param("followingTotal") int followingTotal);


    /**
     * 更新 t_note_count 计数表笔记点赞数
     */
    int updateNoteLikeTotalByUserId(@Param("noteId") long noteId,
                                    @Param("noteLikeTotal") int noteLikeTotal);
}
