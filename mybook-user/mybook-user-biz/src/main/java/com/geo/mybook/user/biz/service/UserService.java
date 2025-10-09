package com.geo.mybook.user.biz.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.geo.framework.common.response.Response;
import com.geo.mybook.user.biz.domain.po.User;
import com.geo.mybook.user.biz.domain.vo.UpdateUserInfoReqVo;
import com.geo.mybook.user.dto.req.*;
import com.geo.mybook.user.dto.res.FindUserByAccountResDTO;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;

import java.util.List;

/*
creator：AZERL7
createTime：11:53
*/
public interface UserService extends IService<User> {

    /**
     * 更新用户信息
     * @param updateUserInfoReqVo 更新的用户信息
     * @return response
     */
    Response<String> updateUserInfo(UpdateUserInfoReqVo updateUserInfoReqVo);


    /**
     * 用户注册需要的api
     * @param registerUserDTO 用户注册需要的api
     * @return response<long>
     */
    Response<Long> register(RegisterUserDTO registerUserDTO);


    /**
     * 根据手机号查询用户信息
     * @param findUserByAccountReqDTO 查询手机号需要的req
     * @return response
     */
    Response<FindUserByAccountResDTO> findByAccount(FindUserByAccountReqDTO findUserByAccountReqDTO);


    /**
     * 修改密码
     * @param updateUserPasswordReqDTO 修改密码需要的req
     * @return response
     */
    Response<String> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    /**
     * 查询用户信息返回给前端获取头像和昵称
     * @param findUserByIdReqDTO 根据id查询用户信息
     * @return response
     */
    Response<FindUserByIdResDTO> findById2nickname2avatar(FindUserByIdReqDTO findUserByIdReqDTO);


    /**
     * 根据id查找用户信息
     * @param findUserByIdReqDTO 根据id查找用户信息
     * @return response
     */
    Response<User> findById(FindUserByIdReqDTO findUserByIdReqDTO);

    Response<List<FindUserByIdResDTO>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO);
}
