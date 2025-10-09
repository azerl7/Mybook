package com.geo.mybook.data.align.constants;


/*
creator：AZERL7
createTime：15:30
*/
public class TableConstants {
    private static final String TABLE_NAME_SEPARATOR = "_";

    public static String buildTableNameSuffix(String date,long hashKey){//多次简单计算使用基本类型，避免浪费性能
        return date+TABLE_NAME_SEPARATOR+hashKey;
    }
}
