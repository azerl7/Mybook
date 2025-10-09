package com.geo.mybook.auth.alarm.impl;


import com.geo.mybook.auth.alarm.AlarmInterface;
import lombok.extern.slf4j.Slf4j;

/*
creator：AZERL7
createTime：14:53
*/
@Slf4j
public class SmsAlarmHelper implements AlarmInterface {
    @Override
    public boolean send(String message) {
        log.info("==> 【短信警告】：{}",message);
        //todo
        return true;
    }
}
