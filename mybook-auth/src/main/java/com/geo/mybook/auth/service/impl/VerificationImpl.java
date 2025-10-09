package com.geo.mybook.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.geo.framework.common.exception.BizException;
import com.geo.framework.common.response.Response;
import com.geo.framework.common.util.EmailSender;
import com.geo.mybook.auth.domain.vo.EmailVerificationVo;
import com.geo.mybook.auth.domain.vo.PhoneVerificationVo;
import com.geo.mybook.auth.domain.vo.VerificationVo;
import com.geo.mybook.auth.enums.ResponseCodeEnum;
import com.geo.mybook.auth.service.VerificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.geo.framework.common.util.Constants.*;

/*
creator：AZERL7
createTime：17:24
*/
@Slf4j
@Service
public class VerificationImpl implements VerificationService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource(name="taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Response<?> send(PhoneVerificationVo verification) {

        try{
            String code=sendBase(verification);
            //4、异步发送验证码
            //todo调用第三方短信发送服务
            threadPoolTaskExecutor.submit(()->{
                log.info("code: {},phone: {}",code,verification.getPhone());
            });
        }catch(Exception e){
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        return Response.success();
    }

    @Override
    public Response<?> send(EmailVerificationVo verification) {
        //0、验证邮箱格式是否正确
        try {
            String code = sendBase(verification);
            //4、异步发送邮件
            threadPoolTaskExecutor.submit(()->{
                try {
                    EmailSender.builder()
                            .smtpHost(EMAIL_SMTP_HOST)
                            .fromEmail(EMAIL_FROM_EMAIL)
                            .authCode(EMAIL_AUTH_CODE)
                            .build().sendVerificationCode(verification.getEmail(),code);
                }catch (Exception e){
                    throw new BizException(ResponseCodeEnum.SYSTEM_ERROR);
                }
            });
        } catch (BizException e) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        return Response.success();
    }

    private String sendBase(VerificationVo verification){
        //因为验证码时间很短，重复了也无所谓，6位数字验证码够破解很久了
        // ，而且请求次数有限制，所以这里暂时可以不用考虑验证码重复性，只需要简单验证即可了吗。。
        //不知道以后会不会有“好家伙”出来对这玩意安全性进行考验
        //1、获取内容
        String data=verification.getData();
        //2、创建对应的key
        String dataKey=VERIFICATION_DATA_KEY+data;
        String codeKey=VERIFICATION_CODE_KEY+data;
        //3、检查是否已经发送过验证码了，（一分钟内）

        //3.1、已经发送过了（一分钟内）返回请求频繁
        if(stringRedisTemplate.hasKey(dataKey)){
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        //3.2、没发送则进行发送，（直接覆盖redis即可）
        //3.2.1、存储账号值
        stringRedisTemplate.opsForValue().set(dataKey,"1",VERIFICATION_DATA_TTL, TimeUnit.MILLISECONDS);
        //3.2.2、存储验证码
        String code= RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(codeKey,code,VERIFICATION_CODE_TTL,TimeUnit.MILLISECONDS);
        return code;
    }
}
