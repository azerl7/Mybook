package com.geo.mybook.auth;


import com.alibaba.druid.filter.config.ConfigTools;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/*
creator：AZERL7
createTime：11:21
*/
@Slf4j
@SpringBootTest
public class DruidTest {

    @Test
    @SneakyThrows
    void EncodePassword(){
        String password="062690";
        String[] arr= ConfigTools.genKeyPair(512);
        System.out.println("RSA public key :"+arr[0]);
        System.out.println("RSA private key :"+arr[1]);
        String s = ConfigTools.encrypt(arr[0],password);
        System.out.println(s);
        //公钥用于加密，私钥用于解密
    }
}
