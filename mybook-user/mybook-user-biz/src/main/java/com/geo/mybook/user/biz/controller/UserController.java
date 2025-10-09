package com.geo.mybook.user.biz.controller;


import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.framework.common.response.Response;
import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import com.geo.mybook.user.biz.domain.po.User;
import com.geo.mybook.user.biz.domain.vo.UpdateUserInfoReqVo;
import com.geo.mybook.user.biz.service.UserService;
import com.geo.mybook.user.dto.req.*;
import com.geo.mybook.user.dto.res.FindUserByAccountResDTO;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import jakarta.annotation.Resource;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
creator：AZERL7
createTime：11:52
*/
@RestController
//@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 更新用户信息
     * @param updateUserInfoReqVo 需要更新的用户信息
     * @return response
     */
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<String> updateUserInfo(UpdateUserInfoReqVo updateUserInfoReqVo){
        return userService.updateUserInfo(updateUserInfoReqVo);
    }

    /**
     * 用户注册
     * @param registerUserDTO 注册需要的dto
     * @return response
     */
    @PostMapping("/register")
    @ApiOperationLog(description = "用户注册")
    public Response<Long> register(@RequestBody @Validated RegisterUserDTO registerUserDTO){
//        System.out.println(registerUserDTO+" dto");
        return userService.register(registerUserDTO);
    }


    /**
     * 根据account查询用户id和**
     * @param findUserByAccountReqDTO 查询用户基本信息
     * @return response
     */
    @PostMapping("/findByAccount")
    @ApiOperationLog
    public Response<FindUserByAccountResDTO> findByAccount(@RequestBody @Validated FindUserByAccountReqDTO findUserByAccountReqDTO){
        return userService.findByAccount(findUserByAccountReqDTO);
    }

    /**
     * 更新用户密码
     * @param updateUserPasswordReqDTO 更新用户密码dto
     * @return response
     */
    @PostMapping("/password/update")
    @ApiOperationLog(description = "密码更新")
    public Response<String> updatePassword(@RequestBody @Validated UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        return userService.updatePassword(updateUserPasswordReqDTO);
    }

    /**
     * 查询用户信息
     * @param findUserByIdReqDTO 查询用户信息id
     * @return response
     */
    @PostMapping("/user/profile")
    @ApiOperationLog(description = "查询用户")
    public Response<User> findById(@RequestBody @Validated FindUserByIdReqDTO findUserByIdReqDTO){
        if(ObjectUtil.isNotNull(findUserByIdReqDTO.getUserId())){
            return userService.findById(findUserByIdReqDTO);
        }else{
            findUserByIdReqDTO.setUserId(LoginUserContextHolder.getUserId());
            return userService.findById(findUserByIdReqDTO);
        }
    }

    /**
     * 查询用户头像和昵称
     * @param findUserByIdReqDTO 查询用户头像和昵称
     * @return response
     */
    @PostMapping("/findById2nickname2avatar")
    @ApiOperationLog(description = "查询用户头像和昵称")
    public Response<FindUserByIdResDTO> findById2nickname2avatar(@RequestBody @Validated FindUserByIdReqDTO  findUserByIdReqDTO){
        return userService.findById2nickname2avatar(findUserByIdReqDTO);
    }

    /**
     * 根据用户id批量查询信息
     * @param findUsersByIdsReqDTO 批量查询信息
     * @return response
     */
    @PostMapping("/findByIds")
    @ApiOperationLog(description = "批量查询用户信息")
    public Response<List<FindUserByIdResDTO>> findByIds(@RequestBody @Validated FindUsersByIdsReqDTO findUsersByIdsReqDTO){
        return userService.findByIds(findUsersByIdsReqDTO);
    }

}
