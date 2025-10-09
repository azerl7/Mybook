package com.geo.framework.common.util;

import cn.hutool.crypto.digest.BCrypt;
import io.micrometer.common.util.StringUtils;

import static com.geo.framework.common.util.Constants.PASSWORD_SALT_LENGTH;

/*
creator：AZERL7
createTime：11:22
*/
public class BCryptUtils {

    /**
     * bcrypt加密
     * @param password 需要加密的密码
     * @return 加密之后的密码
     */
    public static String encrypt(String password){
        if(password==null||password.trim().isEmpty()){
            throw new IllegalArgumentException("加密密码不能为空");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(PASSWORD_SALT_LENGTH));
    }


    /**
     * 密码验证
     * @param password 需要验证的密码
     * @param hashPassword 加密后的密码
     * @return 验证后的结构
     */
    public static boolean verify(String password,String hashPassword){
        if(password==null||password.trim().isEmpty()||
        hashPassword==null||hashPassword.trim().isEmpty()){
            return false;
        }
        try{
//            System.out.println(password);
//            System.out.println(hashPassword);
            return BCrypt.checkpw(password,hashPassword);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
