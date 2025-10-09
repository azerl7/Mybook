package com.geo.mybook.auth.alarm;


/*
creator：AZERL7
createTime：14:52
*/

public interface AlarmInterface {

    /**
     * 发送警告消息
     * @param message 消息
     * @return boolean
     */
    boolean send(String message);
}
