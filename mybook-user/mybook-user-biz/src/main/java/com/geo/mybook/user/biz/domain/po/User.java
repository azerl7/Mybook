package com.geo.mybook.user.biz.domain.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
creator：AZERL7
createTime：11:57
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User {

    private Long id;

    private String mybookId;

    private String password;

    private String nickname;

    private String avatar;

    private LocalDate birthday;

    private String backgroundImg;

    private String email;

    private String phone;

    private Byte sex;

    private Byte status;

    private String introduction;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean isDeleted;
}
