package com.geo.mybook.search.service;


import com.geo.framework.common.response.PageResponse;
import com.geo.mybook.search.domain.vo.SearchUserReqVO;
import com.geo.mybook.search.domain.vo.SearchUserResVO;

/*
creator：AZERL7
createTime：15:47
*/
public interface UserService {
    /**
     * 搜索用户
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserResVO> searchUser(SearchUserReqVO searchUserReqVO);
}
