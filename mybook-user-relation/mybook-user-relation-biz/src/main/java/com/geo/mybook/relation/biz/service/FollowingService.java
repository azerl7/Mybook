package com.geo.mybook.relation.biz.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.geo.framework.common.response.PageResponse;
import com.geo.framework.common.response.Response;
import com.geo.mybook.relation.biz.domain.po.Following;
import com.geo.mybook.relation.biz.domain.vo.FindFollowingListReqVo;
import com.geo.mybook.relation.biz.domain.vo.FindFollowingUserResVo;
import com.geo.mybook.relation.biz.domain.vo.FollowUserReqVo;
import com.geo.mybook.relation.biz.domain.vo.UnfollowUserReqVo;

/*
creator：AZERL7
createTime：14:37
*/
public interface FollowingService extends IService<Following> {

    /**
     * 关注用户
     * @param followUserReqVo followUserReqVO
     * @return response
     */
    Response<?> follow(FollowUserReqVo followUserReqVo);

    Response<?> unfollow(UnfollowUserReqVo unfollowUserReqVo);

    /**
     * 查询关注列表
     * @param findFollowingListReqVo findFollowingListReqVo
     * @return pageResponse
     */
    PageResponse<FindFollowingUserResVo> findFollowingList(FindFollowingListReqVo findFollowingListReqVo);
}
