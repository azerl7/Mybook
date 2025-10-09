package com.geo.mybook.relation.biz.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.geo.framework.common.response.PageResponse;
import com.geo.mybook.relation.biz.domain.po.Fans;
import com.geo.mybook.relation.biz.domain.vo.FindFansListReqVo;
import com.geo.mybook.relation.biz.domain.vo.FindFansUserResVo;


/*
creator：AZERL7
createTime：14:34
*/

public interface FansService extends IService<Fans> {

    /**
     * 查询粉丝列表
     * @param findFansListReqVo 查询粉丝列表所需的req
     * @return pageresponse
     */
    PageResponse<FindFansUserResVo> findFansList(FindFansListReqVo findFansListReqVo);
}
