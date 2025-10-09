package com.geo.mybook.search.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/*
creator：AZERL7
createTime：17:08
*/
@Getter
@AllArgsConstructor
public enum NoteSortTypeEnum {

    // 最新
    LATEST(0),
    // 最新点赞
    MOST_LIKE(1),
    // 最多评论
    MOST_COMMENT(2),
    // 最多收藏
    MOST_COLLECT(3),
    ;

    private final Integer code;

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code 值
     * @return type
     */
    public static NoteSortTypeEnum valueOf(Integer code) {
        for (NoteSortTypeEnum noteSortTypeEnum : NoteSortTypeEnum.values()) {
            if (Objects.equals(code, noteSortTypeEnum.getCode())) {
                return noteSortTypeEnum;
            }
        }
        return null;
    }
}
