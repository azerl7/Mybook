package com.geo.mybook.auth.enums;


import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.cfg.EnumFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/*
creator：AZERL7
createTime：16:47
*/
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {
    /**
     * 验证码
     */
    VERIFICATION_CODE(1),
    PASSWORD(2),
    ;

    private final Integer value;

//    static boolean check(int x){//有时候把脑子方简单点反而更加方便，脑子才能更加灵活，而且比起死板来说更加轻松
//        return true;
//    }
    public static LoginTypeEnum valueOf(Integer code){
        if(code==null){
            return null;
        }
        LoginTypeEnum[] values=LoginTypeEnum.values();
//        //浅浅练习一下二分查找咯//555，写得不对重新来写
//        int l=0,r=LoginTypeEnum.values().length;
//        while(l<r-1){
//            int mid=l+r>>1;
//            LoginTypeEnum t=values[mid];
//            if(t.value.equals(code)){
//                return t;
//            }else if(t.value<code){
//                l=mid;
//            }else{
//                r=mid;
//            }
//        }
        for (LoginTypeEnum loginTypeEnum : LoginTypeEnum.values()) {
            if (ObjectUtil.equal(code, loginTypeEnum.getValue())) {
                return loginTypeEnum;
            }
        }
        return null;
    }
}
