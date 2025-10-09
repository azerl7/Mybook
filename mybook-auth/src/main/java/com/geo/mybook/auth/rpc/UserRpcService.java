package com.geo.mybook.auth.rpc;


import com.geo.framework.common.response.Response;
import com.geo.mybook.user.api.UserFeignApi;
import com.geo.mybook.user.dto.req.FindUserByAccountReqDTO;
import com.geo.mybook.user.dto.req.RegisterUserDTO;
import com.geo.mybook.user.dto.req.UpdateUserPasswordReqDTO;
import com.geo.mybook.user.dto.res.FindUserByAccountResDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
creator：AZERL7
createTime：18:04
*/
@Component
public class UserRpcService {

    @Autowired
    private UserFeignApi userFeignApi;

    /**
     * 用户注册
     * @param account 用户账户
     * @return userId
     */
    public Long register(String account){
        RegisterUserDTO registerUserDTO=new RegisterUserDTO();
        registerUserDTO.setAccount(account);
//        System.out.println(account+" account");
        System.out.println(registerUserDTO);
        Response<Long> response=userFeignApi.register(registerUserDTO);
        if(!response.isSuccess()){
            return null;
        }
        return response.getData();
    }


    public FindUserByAccountResDTO findUserByAccount(String account){
        FindUserByAccountReqDTO findUserByAccountReqDTO=FindUserByAccountReqDTO.builder().account(account).build();
        Response<FindUserByAccountResDTO> response=userFeignApi.findByAccount(findUserByAccountReqDTO);
        if(!response.isSuccess()){
            return null;
        }
        return response.getData();
    }

    /**
     * 更新密码
     * @param encodePassword 加密后的密码
     */
    public void updatePassword(String encodePassword) {
        UpdateUserPasswordReqDTO updateUserPasswordReqDTO =UpdateUserPasswordReqDTO.builder()
                        .encodePassword(encodePassword).build();
        userFeignApi.updatePassword(updateUserPasswordReqDTO);
    }
}
