package com.tongyi.rescue_api.common.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateTimeZoneUtil {

    public interface Format {

        String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

        String DATE_TIME_WHEN = "yyyy-MM-dd HH:mm";

        String DATE = "yyyy-MM-dd";

        String SLASH_DATE = "yyyyMMdd";

        String TIME = "HH:mm:ss";
    }

    /**
     * 时间转 TimeZone, 加上指定的分钟
     * <p>
     * 2021-08-16T14:40:25+08:00
     *
     * @param minute 分钟
     * @return {@link String}  TimeZone 格式时间字符串
     */
    public static String dateToTimeZoneMinute(Integer minute) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, minute);
        return DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(instance);
    }

    /**
     * TimeZone 转成时间
     *
     * @param dateStr 字符串格式的时间 2021-08-18T17:13:00+08:00
     * @return {@link Date}
     */
    public static Date timeZoneToDate(String dateStr) {
        try {
            return DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Date 类型 转成 LocalDateTime 类型
     *
     * @param date {@link Date}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (null != date) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            return LocalDateTime.ofInstant(instant, zoneId);
        }
        return null;
    }

    /**
     * 指定当前时间的格式
     * <p>
     * 20210818
     *
     * @return {@link String}  yyyyMMdd 格式时间字符串
     */
    public static String date() {
        Calendar instance = Calendar.getInstance();
        return DateFormatUtils.format(instance, Format.SLASH_DATE);
    }

    /**
     * 指定当前时间的格式
     * <p>
     *
     * @return {@link String}  格式时间字符串
     */
    public static String date(String format) {
        Calendar instance = Calendar.getInstance();
        return DateFormatUtils.format(instance, format);
    }

    /**
     * 指定时间的指定格式
     * <p>
     *
     * @return {@link String}  格式时间字符串
     */
    public static String appointDate(Calendar instance, String format) {
        return DateFormatUtils.format(instance, format);
    }

    /**
     * 指定时间的格式
     * <p>
     * 20210818
     *
     * @param instance 时间
     * @return {@link String}  yyyyMMdd 格式时间字符串
     */
    public static String date(Calendar instance) {
        return DateFormatUtils.format(instance, Format.SLASH_DATE);
    }

    /**
     * 当前时间减去指定的分钟
     *
     * @param minute 分钟
     * @return {@link String}  yyyyMMdd 格式时间字符串
     */
    public static LocalDateTime dateSubMinute(int minute) {
        ZoneId zoneId = ZoneId.systemDefault();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, -minute);
        Instant instant = instance.toInstant();
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * 获取当前月的剩余秒数
     *
     * @return 剩余的秒数
     */
    public static long monthSurplusSecondTotal() {
        Calendar calendar = Calendar.getInstance();
        // 当前时间的秒数
        long currTimeInMillis = calendar.getTimeInMillis() / 1000;
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long timestamp = calendar.getTimeInMillis() / 1000;
        return timestamp - currTimeInMillis;
    }

    /**
     * 将字符串转成时间
     *
     * @param time   时间字符串
     * @param format 指定的格式
     * @return 时间对象
     */
    public static LocalDateTime parseStringToDateTime(String time, String format) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(time, df);
    }

    /**
     * 将时间转成字符串
     *
     * @param dateTime 时间对象
     * @param format   指定的格式
     * @return 时间字符串
     */
    public static String parseDateTimeToString(LocalDateTime dateTime, String format) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return df.format(dateTime);
    }

    /**
     * 当前时间加上指定的天
     *
     * @param day 天数
     * @return 时间对象
     */
    public static Calendar addCalendarDay(int day) {
        Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.add(Calendar.DATE, day);
        return calendar;
    }

    public static void main(String[] args) {
        long l = monthSurplusSecondTotal();
        System.err.println(l);
        //Date date = timeZoneToDate("2021-09-03T16:32:16.102");
        //System.err.println(date);
        //
        Calendar calendar = Calendar.getInstance();

        System.err.println(appointDate(addCalendarDay(60), Format.DATE_TIME));

        System.err.println(parseDateTimeToString(LocalDateTime.now(), Format.DATE_TIME));
    }

}




