package com.geo.mybook.note.biz.rpc;


import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.common.response.Response;
import com.geo.mybook.user.api.UserFeignApi;
import com.geo.mybook.user.dto.req.FindUserByIdReqDTO;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/*
creator：AZERL7
createTime：16:41
*/
@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;


    public FindUserByIdResDTO findById2Nickname2Avatar(Long userId){
        FindUserByIdReqDTO findUserByIdReqDTO=FindUserByIdReqDTO.builder()
                .userId(userId)
                .build();

        Response<FindUserByIdResDTO> response=userFeignApi.findById2nickname2avatar(findUserByIdReqDTO);

        if(ObjectUtil.isNull(response)||!response.isSuccess()){
            return null;
        }

        return response.getData();
    }
}
