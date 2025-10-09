package com.geo.mybook.relation.biz.rpc;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.geo.framework.common.response.Response;
import com.geo.mybook.user.api.UserFeignApi;
import com.geo.mybook.user.dto.req.FindUserByIdReqDTO;
import com.geo.mybook.user.dto.req.FindUsersByIdsReqDTO;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/*
creator：AZERL7
createTime：16:17
*/
@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    public FindUserByIdResDTO findById(Long userId){
        FindUserByIdReqDTO findUserByIdReqDTO=new FindUserByIdReqDTO();
        findUserByIdReqDTO.setUserId(userId);
        Response<FindUserByIdResDTO> response=userFeignApi.findById2nickname2avatar(findUserByIdReqDTO);
        //只需要校验存在与否不用传递太多的参数，仅仅用于验证是否存在即可，为了方便使用查询昵称和头像代替
        if(!response.isSuccess()|| ObjectUtil.isNull(response.getData())){
            return null;
        }
        return response.getData();
    }


    public List<FindUserByIdResDTO> findByIds(List<Long> userIds){
        FindUsersByIdsReqDTO findUsersByIdsReqDTO=new FindUsersByIdsReqDTO();
        findUsersByIdsReqDTO.setIds(userIds);
        Response<List<FindUserByIdResDTO>> response=userFeignApi.findByIds(findUsersByIdsReqDTO);

        if(!response.isSuccess()||ObjectUtil.isNull(response.getData())|| CollectionUtil.isEmpty(response.getData())){
            return null;
        }

        return response.getData();
    }
}
