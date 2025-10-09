package com.geo.framework.common.util;


/*
creator：AZERL7
createTime：12:12
*/
public class MathUtils {
    private MathUtils(){}

    /**
     * 用来判断某一个数字是否在一个范围内【左闭右闭】
     * @param number 需要判断的数字
     * @param min 范围最小值
     * @param max 范围最大值
     * @return boolean
     * @param <T> 数值类型
     */
    public static <T extends Number> boolean isInRange(T number,T min,T max){
        if (number == null || min == null || max == null) {
            throw new IllegalArgumentException("参数不能为null");
        }

        double num = number.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();

        // 确保min不大于max
        if (minVal > maxVal) {
            double temp = minVal;
            minVal = maxVal;
            maxVal = temp;
        }

        return num >= minVal && num <= maxVal;
    }
}
