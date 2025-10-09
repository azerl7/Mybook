package com.geo.mybook.relation.biz.controller;


import cn.hutool.db.Page;
import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.framework.common.response.PageResponse;
import com.geo.framework.common.response.Response;
import com.geo.mybook.relation.biz.domain.vo.*;
import com.geo.mybook.relation.biz.service.FansService;
import com.geo.mybook.relation.biz.service.FollowingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
creator：AZERL7
createTime：16:31
*/
@Slf4j
@RestController
//@RequestMapping("/relation")
public class RelationController {

    @Resource
    private FollowingService followingService;

    @Resource
    private FansService fansService;

    /**
     * 关注
     * @param followUserReqVo followUserReqVo
     * @return response
     */
    @PostMapping("/follow")
    @ApiOperationLog(description = "关注用户")
    public Response<?> follow(@RequestBody @Validated FollowUserReqVo followUserReqVo){
        return followingService.follow(followUserReqVo);
    }

    /**
     * 取关
     * @param unfollowUserReqVo unfollowUserReqVo
     * @return response
     */
    @PostMapping("/unfollow")
    @ApiOperationLog(description = "取关用户")
    public Response<?> unfollow(@RequestBody @Validated UnfollowUserReqVo unfollowUserReqVo){
        return followingService.unfollow(unfollowUserReqVo);
    }


    /**
     * 查询用户关注列表
     * @param findFollowingListReqVo 用户关注列表
     * @return pageResponse
     */
    @PostMapping("/following/list")
    @ApiOperationLog(description = "查询用户关注列表")
    public PageResponse<FindFollowingUserResVo> findFollowingList(@RequestBody @Validated FindFollowingListReqVo findFollowingListReqVo){
        return followingService.findFollowingList(findFollowingListReqVo);
    }


    /**
     * 查询用户粉丝列表
     * @param findFansListReqVo 查询用户粉丝请求体
     * @return pageResponse
     */
    @PostMapping("/fans/list")
    @ApiOperationLog(description = "查询用户粉丝列表")
    public PageResponse<FindFansUserResVo> findFansList(@RequestBody @Validated FindFansListReqVo findFansListReqVo){
        return fansService.findFansList(findFansListReqVo);
    }
}
