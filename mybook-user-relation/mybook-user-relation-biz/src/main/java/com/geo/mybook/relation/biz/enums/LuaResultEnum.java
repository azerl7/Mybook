package com.geo.mybook.relation.biz.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
creator：AZERL7
createTime：17:28
*/
@Getter
@AllArgsConstructor
public enum LuaResultEnum {
    //ZSET不存在
    ZSET_NOT_EXISTS(-1L),
    //关注已达上限
    FOLLOW_LIMIT(-2L),
    //已经关注了
    ALREADY_FOLLOWED(-3L),
    //未关注该用户
    NOT_FOLLOWED(-4L),
    //关注成功
    FOLLOW_SUCCESS(0L),
    ;

    private final Long code;
    public static LuaResultEnum valueOf(Long code){
        for(LuaResultEnum luaResultEnum:LuaResultEnum.values()){
            if(ObjectUtil.equals(code,luaResultEnum.getCode())){
                return luaResultEnum;
            }
        }
        return null;
    }
}
