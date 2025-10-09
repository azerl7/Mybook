package com.geo.mybook.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geo.framework.common.exception.BizException;
import com.geo.framework.common.response.Response;
import com.geo.framework.common.util.BCryptUtils;
import com.geo.framework.common.util.JsonUtils;
import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import com.geo.mybook.auth.domain.po.User;
import com.geo.mybook.auth.domain.vo.UpdatePasswordReqVo;
import com.geo.mybook.auth.domain.vo.UserEmailLoginReqVo;
import com.geo.mybook.auth.domain.vo.UserPhoneLoginReqVo;
import com.geo.mybook.auth.enums.LoginTypeEnum;
import com.geo.mybook.auth.enums.ResponseCodeEnum;
import com.geo.mybook.auth.mapper.UserMapper;
import com.geo.mybook.auth.rpc.UserRpcService;
import com.geo.mybook.auth.service.AuthService;
import com.geo.mybook.user.api.UserFeignApi;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;

import static com.geo.framework.common.util.Constants.*;


/*
creator：AZERL7
createTime：9:50
*/
@Slf4j
@Service
public class  AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private UserMapper userMapper;


    @Autowired
    private UserRpcService userRpcService;


    @Override
    public Response<String> login(UserPhoneLoginReqVo userLoginReqVo) {
        //1、判断登录类型
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(userLoginReqVo.getType());
        Long userId=null;

        //2、根据登录类型进行登录
        switch(loginTypeEnum){
            case VERIFICATION_CODE -> {
                userId=verificationCodeLogin(
                        userLoginReqVo.getPhone(),
                        userLoginReqVo.getCode()
                );
                break;
            }
            case PASSWORD -> {
                //todo
                userId=passwordLogin(
                        userLoginReqVo.getPhone(),
                        userLoginReqVo.getPassword()
                );
                break;
            }
        }
        //3、判断是否存在用户
        //3.1、存在当前用户，进行登录验证，如果是密码登录，验证密码，如果是验证码登录，验证验证码
        //3.2、不存在当前用户只以验证码作为校验，验证码正确创建用户
        //4、返回sa-token登录获取token并返回给前端
        StpUtil.login(userId);
        SaTokenInfo tokenInfo=StpUtil.getTokenInfo();
        return Response.success(tokenInfo.tokenValue);
    }

    @Override
    public Response<String> login(UserEmailLoginReqVo userLoginReqVo) {
        //1、获取登录类型
        LoginTypeEnum loginTypeEnum=LoginTypeEnum.valueOf(userLoginReqVo.getType());
        Long userId=null;

//        System.out.println(userLoginReqVo);

        //2、根据登录类型进行登录
        switch(loginTypeEnum){
            case VERIFICATION_CODE -> {
                userId=verificationCodeLogin(
                        userLoginReqVo.getEmail(),
                        userLoginReqVo.getCode()
                );
            }
            case PASSWORD -> {
                //todo
                userId=passwordLogin(
                        userLoginReqVo.getEmail(),
                        userLoginReqVo.getPassword()
                );
                break;
            }
        }
        //3、存入sa-token并且返回token给前端
        StpUtil.login(userId);
        SaTokenInfo tokenInfo=StpUtil.getTokenInfo();
        return Response.success(tokenInfo.tokenValue);
    }


    @Override
    public Response<String> logout(Long userId){
        StpUtil.logout(userId);
        return Response.success();
    }

    @Override
    public Response<String> updatePassword(UpdatePasswordReqVo updatePasswordReqVo) {
        Long userId=LoginUserContextHolder.getUserId();
        if (ObjectUtil.isNull(userId)) {
            throw new BizException(ResponseCodeEnum.NOT_LOGIN); // 需在枚举中添加"未登录"状态码
        }
        //1、初始化
        String newPassword=updatePasswordReqVo.getNewPassword();
        String encodePassword= BCryptUtils.encrypt(newPassword);

        //2、调用user服务进行跟新
        userRpcService.updatePassword(encodePassword);
        return Response.success();
    }

    /**
     * 验证码登录
     * @param account 账号
     * @param code 验证码
     * @return 用户id
     */
    private Long verificationCodeLogin(String account,String code){
        //2.1、判断验证码是否为空
//        if(StringUtils.isBlank(code)){
//            throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
//        }
        //使用guava校验
        Preconditions.checkArgument(StringUtils.isNotBlank(code), "验证码不能为空");
        //2.2、判断用户输入的验证码和redis中的验证码是否一致
        String key=VERIFICATION_CODE_KEY+account;
        String sentCode=stringRedisTemplate.opsForValue().get(key);

        if(!StringUtils.equals(code,sentCode)){//验证码不一致
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }
        //2.3、判断用户是否已经注册
        User user= BeanUtil.copyProperties(userRpcService.findUserByAccount(account),User.class);
        log.info("用户是否已经注册，account{}，user{}",account,JsonUtils.toJsonString(user));
        Long userId=null;
        if(user==null){
//            System.out.println(account);
            userId= userRpcService.register(account);
            if(ObjectUtil.isNull(userId)){//因为是别人写的服务，所以不能全信
                throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
            }
        }else{
            userId=user.getId();
        }
        return userId;
    }


    private Long passwordLogin(String account,String password){
        //1、判断用户是否已经注册
        User user=BeanUtil.copyProperties(userRpcService.findUserByAccount(account),User.class);
        if(ObjectUtil.isNull(user)){
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        String encodePassword=user.getPassword();
        if(!BCryptUtils.verify(password,encodePassword)){
            throw new  BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
        }
        return user.getId();
    }

}
