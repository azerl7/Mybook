package com.geo.framework.common.util;


import java.math.RoundingMode;
import java.text.DecimalFormat;

/*
creator：AZERL7
createTime：16:23
*/
public class NumberUtils {
    /**
     * 数字转换字符串
     *
     * @param number 数字
     * @return numberString 数字中文单位字符串
     */
    public static String formatNumberString(long number) {
        if (number < 10000) {
            return String.valueOf(number);  // 小于 1 万显示原始数字
        } else if (number >= 10000 && number < 100000000) {//没有优化是为了代码可读性
            // 小于 1 亿，显示万单位
            double result = number / 10000.0;
            DecimalFormat df = new DecimalFormat("#.#"); // 保留 1 位小数
            df.setRoundingMode(RoundingMode.DOWN); // 禁用四舍五入
            String formatted = df.format(result);
            return formatted + "万";
        } else {
            return "9999万";  // 超过 1 亿，统一显示 9999万
        }
    }
}
