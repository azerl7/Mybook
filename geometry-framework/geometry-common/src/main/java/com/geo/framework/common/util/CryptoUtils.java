package com.geo.framework.common.util;


import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;


/*
creator：AZERL7
createTime：18:03
*/
public class CryptoUtils {
    private final RSA rsa = new RSA();
    private final String privateKey;
    private final String publicKey;

    public CryptoUtils() {
        privateKey = rsa.getPrivateKeyBase64();
        publicKey = rsa.getPublicKeyBase64();
    }

    //获取RSA公钥
    public  String getPublicKey() {
        System.out.println("RSA private key :"+privateKey);//返回公钥，为了内部方便调试输出私钥
        return publicKey;
    }

    //RSA加密（公钥）
    public  String rsaEncrypt(String data){
        return rsa.encryptBase64(data, KeyType.PublicKey);
    }

    //RSA解密（私钥）
    public String rsaDecrypt(String data){
        return rsa.decryptStr(data, KeyType.PrivateKey);
    }

    //AES解密 cbc模式 pkcs5填充
    public String aesDecrypt(String data,String key,String iv){
        AES ase=new AES(Mode.CBC, Padding.PKCS5Padding,key.getBytes(),iv.getBytes());
        return ase.decryptStr(data);
    }

    //AES加密 cbc模式 pkcs5填充
    public String aesEncrypt(String data,String key,String iv){
        AES ase=new AES(Mode.CBC, Padding.PKCS5Padding,key.getBytes(),iv.getBytes());
        return ase.encryptBase64(data);
    }
}
