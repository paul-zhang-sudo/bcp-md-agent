package com.bsi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 * @author fish
 */
public class DateUtils {

    /**
     * 获取指定格式的当前时间字符串
     * @param pattern
     * @return String
     */
    public static String nowDate(String pattern){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        return df.format( LocalDateTime.now() );
    }

    /**
     * 获取当前日期多少分钟之前的日期，为负数可以获取多少分钟之后的数据
     * @param pattern
     * @param minute
     * @return String
     */
    public static String preMinuteForNow(Long minute,String pattern){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        return df.format( LocalDateTime.now().minusMinutes(minute));
    }

    /**
     * 获取当前日期多少秒之前的日期，为负数可以获取多少秒之后的数据
     * @param pattern
     * @param minute
     * @return String
     */
    public static String preSecondsForNow(Long minute,String pattern){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        return df.format( LocalDateTime.now().minusSeconds(minute));
    }
}
