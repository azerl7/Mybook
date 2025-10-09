package com.geo.framework.common.util;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.security.Security;

import static com.geo.framework.common.util.Constants.SM3_SALT_LENGTH;

/*
creator：AZERL7
createTime：14:23
*/

/// ///////////////////////////////////////////////
public class SM3Utils {
    static{
        //加入BouncyCastleProvider支持
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密
     * @param data 需要加密的数据
     * @param key 加密密钥
     * @return sm3密文
     */
    public static String encrypt(String data,String key){
        //2、获取字节数组
        //2.1、使用随机盐混淆密码
        String salt=RandomUtil.randomString(SM3_SALT_LENGTH);
        return baseEncrypt(data,key,salt);
    }

    /**
     * 验证
     * @param data 需要验证的数据
     * @param key 密钥
     * @param salt 盐
     * @return sm3密文
     */
    public static String encrypt(String data,String key,String salt){
        return baseEncrypt(data,key,salt);
    }

    /**
     * 验证
     * @param data 需要验证的数据
     * @param key 密钥
     * @param sign 密文
     * @return 验证结果
     */
    public static Boolean verify(String data,String key,String sign){
        //1、取出盐
        if(sign==null||sign.length()<= SM3_SALT_LENGTH){
            return false;
        }
        String salt=sign.substring(sign.length()- SM3_SALT_LENGTH);//包前不包后
        String signReal=sign.substring(0,sign.length()- SM3_SALT_LENGTH);
        //2、验证密文
        String signData=encrypt(data,key,salt);
//        System.out.println("signData:"+signData);
//        System.out.println("signReal:"+signReal);
        return signReal.equals(signData);
    }

    private static String baseEncrypt(String data,String key,String salt){
        //1、验证是否为空null
        if(ObjectUtil.isNull(data)){
            System.out.println("when data is encrypted,the incoming data value is empty 。");
            return null;
        }
        //第二步在生成随机密码的时候才有

        byte[] keyByte=key.getBytes();
        byte[] dataByte=(data+salt).getBytes();
//        System.out.println("dataByte:"+data+ salt);

        //3、创建加密对象
        KeyParameter keyParameter=new KeyParameter(keyByte);
        //4、创建sm3哈希算法实例
        SM3Digest sm3=new SM3Digest();
        //5、初始化哈希消息验证对象，并指定底层算法为sm3
        HMac hMac=new HMac(sm3);

        //6、初始化，密钥计算，获取结果
        hMac.init(keyParameter);
        hMac.update(dataByte,0,dataByte.length);
        byte[] result=new byte[hMac.getMacSize()];
        hMac.doFinal(result,0);
        return Hex.toHexString(result)+salt;
    }
}