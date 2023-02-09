package com.bsi.utils;

import com.bsi.framework.core.utils.Assert;
import com.bsi.framework.core.utils.CalendarUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期工具类
 * @author fish
 */
public class DateUtils{

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
     * 获取当前日期多少天之前的日期，为负数可以获取多少分钟之后的数据
     * @param pattern
     * @param day
     * @return String
     */
    public static String preDayForNow(Long day,String pattern){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        return df.format( LocalDateTime.now().minusDays(day));
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

    /**
     * 把毫秒数转换成指定格式的日期字符串
     * @param time 毫秒数
     * @param pattern 格式
     * @return
     */
    public static String getDateStrFromTime(long time,String pattern){
        Date date = new Date(time);
        return DateUtils.toString(date,pattern);
    }

    /**
     * 获取当前日期多少分钟之前的毫秒数
     * @param minute
     * @return String
     */
    public static long getTimePreMinuteForNow(Long minute){
        return LocalDateTime.now().minusMinutes(minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Date getDate(String dateString,String pattern){
        if(dateString==null){
            return null;
        }
        Date date;
        if(pattern==null){
            date = new Date(Long.parseLong(dateString));
        }else {
            date = toDate(dateString,pattern);
        }
        return date;
    }

    public static String toString(Date date, String pattern) {
        Assert.notNull(date);
        Assert.notNull(pattern);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String toString(Date date) {
        Assert.notNull(date);
        return toString(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date preDays(Date date, int preDays) {
        return preTime(date,5,preDays);
    }

    /**
     * @param date
     * @param field Calendar类中可以拿到
     * @param preDays
     * @return
     */
    public static Date preTime(Date date,int field ,int preDays) {
        GregorianCalendar c1 = new GregorianCalendar();
        c1.setTime(date);
        GregorianCalendar cloneCalendar = (GregorianCalendar)c1.clone();
        cloneCalendar.add(field, -preDays);
        return cloneCalendar.getTime();
    }

    public static Date toDate(String time, String pattern) {
        Assert.notNull(time);
        Assert.notNull(pattern);
        return CalendarUtils.toCalendar(time, pattern).getTime();
    }
}
