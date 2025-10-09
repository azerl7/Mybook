package com.geo.mybook.search.controller;


import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.framework.common.response.PageResponse;
import com.geo.mybook.search.domain.vo.SearchUserReqVO;
import com.geo.mybook.search.domain.vo.SearchUserResVO;
import com.geo.mybook.search.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
creator：AZERL7
createTime：16:04
*/
@Slf4j
@RestController
@RequestMapping("/search")
public class UserSearchController {
    @Resource
    private UserService userService;

    /**
     * 搜索用户
     * @param searchUserReqVO 搜索用户请求
     * @return pageResponse
     */
    @PostMapping("/user")
    @ApiOperationLog(description = "搜索用户")
    public PageResponse<SearchUserResVO> searchUser(@RequestBody @Validated SearchUserReqVO searchUserReqVO) {
        return userService.searchUser(searchUserReqVO);
    }
}
