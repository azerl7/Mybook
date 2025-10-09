package com.geo.framework.common.util;


/*
creator：AZERL7
createTime：17:10
*/

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.geo.framework.common.util.Constants.*;

/**
 * 获取时间戳的工具类
 */
public class DateUtils {
    /**
     * 时间转换为时间戳
     * @param localDateTime localdate
     * @return timeLong
     */
    public static long localDateTime2TimeStamp(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    /**
     * LocalDateTime 转 String 字符串
     * @param time 需要转换的时间
     * @return String
     */
    public static String localDateTime2String(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * LocalDateTime 转友好的相对时间字符串
     * @param dateTime
     * @return
     */
    public static String formatRelativeTime(LocalDateTime dateTime) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算与当前时间的差距
        long daysDiff = ChronoUnit.DAYS.between(dateTime, now);
        long hoursDiff = ChronoUnit.HOURS.between(dateTime, now);
        long minutesDiff = ChronoUnit.MINUTES.between(dateTime, now);

        if (daysDiff < 1) {  // 如果是今天
            if (hoursDiff < 1) {  // 如果是几分钟前
                return minutesDiff + "分钟前";
            } else {  // 如果是几小时前
                return hoursDiff + "小时前";
            }
        } else if (daysDiff == 1) {  // 如果是昨天
            return "昨天 " + dateTime.format(DATE_FORMAT_H_M);
        } else if (daysDiff < 7) {  // 如果是最近一周
            return daysDiff + "天前";
        } else if (dateTime.getYear() == now.getYear()) {  // 如果是今年
            return dateTime.format(DATE_FORMAT_M_D);
        } else {  // 如果是去年或更早
            return dateTime.format(DATE_FORMAT_Y_M_D);
        }
    }
}
