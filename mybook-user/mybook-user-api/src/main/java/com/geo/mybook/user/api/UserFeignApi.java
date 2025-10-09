package com.geo.mybook.user.api;


import com.geo.framework.common.response.Response;
import com.geo.mybook.user.constant.ApiConstants;
import com.geo.mybook.user.dto.req.*;
import com.geo.mybook.user.dto.res.FindUserByAccountResDTO;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/*
creator：AZERL7
createTime：17:56
*/
@FeignClient(name= ApiConstants.SERVICE_NAME)
public interface UserFeignApi {
    String PREFIX="/user";
    @PostMapping(value="/register")
    Response<Long> register(@RequestBody @Validated RegisterUserDTO registerUserDTO);

    @PostMapping(value =  "/findByAccount")
    Response<FindUserByAccountResDTO> findByAccount(@RequestBody @Validated FindUserByAccountReqDTO findUserByPhoneReqDTO);

    @PostMapping(value = "/password/update")
    Response<String> updatePassword(@RequestBody @Validated UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    @PostMapping(value="/findById")
    Response<?> findById(@RequestBody @Validated FindUserByIdReqDTO findUserByIdReqDTO);

    @PostMapping(value="/findById2nickname2avatar")
    public Response<FindUserByIdResDTO> findById2nickname2avatar(@RequestBody @Validated FindUserByIdReqDTO  findUserByIdReqDTO);

    @PostMapping("/findByIds")
    public Response<List<FindUserByIdResDTO>> findByIds(@RequestBody @Validated FindUsersByIdsReqDTO findUsersByIdsReqDTO);
}