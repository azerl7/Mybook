package com.geo.framework.common.util;


import java.util.regex.Pattern;

/*
creator：AZERL7
createTime：12:02
*/
public class ParamUtils {
    private ParamUtils(){}

    private static final int NICK_NAME_MIN_LENGTH=2;
    private static final int NICK_NAME_MAX_LENGTH=16;

    //防止sql注入或者其他的特殊字符串
    private static final String NICK_NAME_REGEX="[!@#$%^&*(),.?\":{}|<>]";

    /**
     * 昵称校验
     * @param nickName 昵称
     * @return 昵称是否符合规则
     */
    public static boolean checkNickName(String nickName){
        if(nickName==null){
            return false;
        }
        if(!MathUtils.isInRange(nickName.length(),NICK_NAME_MIN_LENGTH,NICK_NAME_MAX_LENGTH)){
            return false;
        }
        Pattern pattern=Pattern.compile(NICK_NAME_REGEX);
        return !pattern.matcher(nickName).find();//不能包含上面的字符
    }

    public static final int ID_MIN_LENGTH=6;
    public static final int ID_MAX_LENGTH=15;
    public static final String ID_REGEX="^[a-zA-Z0-9_]+$";


    /**
     * 检查用户自定义的id是否符合规则
     * @param mybookId 用户自定义id
     * @return boolean
     */
    public static boolean checkMybookId(String mybookId){
        if(mybookId==null){
            return false;
        }
        if(!MathUtils.isInRange(mybookId.length(),ID_MIN_LENGTH,ID_MAX_LENGTH)){
            return false;
        }
        return mybookId.matches(ID_REGEX);
    }

    /**
     * 校验字符串长度
     * @param str 需要校验的字符串
     * @param length 校验长度
     * @return boolean
     */
    public static boolean checkLength(String str,Integer length){
        if(str.isEmpty()||str.length()>length){
            return false;
        }
        return true;
    }

}
