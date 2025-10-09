package com.geo.framework.common.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/*
creator：AZERL7
createTime：10:06
*/
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSender {
    // 网易邮箱SMTP服务器地址
    private String smtpHost ;
    // 发送者邮箱（开通smtp服务的邮箱）
    private String fromEmail;
    // 授权码（16位授权码）
    private String authCode ;


    /**
     * 发送验证码
     * @param email 接收邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    public  boolean sendVerificationCode(String email, String code) throws Exception {
//        System.out.println(smtpHost+"  "+fromEmail+"  "+authCode);

        // 1、配置SMTP服务器属性
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost); // SMTP服务器地址
        props.put("mail.smtp.port", "465"); // 端口（SSL加密使用465）
        props.put("mail.smtp.auth", "true"); // 开启认证
        props.put("mail.smtp.ssl.enable", "true"); // 启用SSL加密
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 指定SSL工厂类

        // 2、创建会话（使用 javax.mail.Session）
        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名是发件人邮箱，密码是授权码
                return new PasswordAuthentication(fromEmail, authCode);
            }
        });

        try {
            // 3、创建邮件消息
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "Mybook", "UTF-8"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("【Mybook】验证码", "UTF-8");
            String content = "你的验证码是：<b>" + code + "</b>，3分钟内有效，请勿泄露给他人。";
            message.setContent(content, "text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();

            // 4、发送邮件
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("邮件发送失败，请检查认证，host，和邮箱");
        }
    }
}
