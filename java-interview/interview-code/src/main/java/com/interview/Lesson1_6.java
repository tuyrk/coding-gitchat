package com.interview;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;

/**
 * 玩转时间操作 + 面试题
 */
public class Lesson1_6 {
    // 【时间操作 JDK 8 之前】
    @Test
    public void jdk8Before() throws ParseException {
        jdk8BeforeGetCurDate();
        jdk8BeforeGetTimestamp();
        jdk8BeforeFormat();
        jdk8BeforeGetYesterday();
    }

    // 获得当前时间
    private void jdk8BeforeGetCurDate() {
        Date date = new Date();
        System.out.println(date);
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();
        System.out.println(time);
    }

    // 获取当前时间戳
    private void jdk8BeforeGetTimestamp() {
        long ts1 = new Date().getTime();
        System.out.println(ts1);
        long ts2 = System.currentTimeMillis();
        System.out.println(ts2);
        long ts3 = Calendar.getInstance().getTimeInMillis();
        System.out.println(ts3);
    }

    // 时间格式化
    private void jdk8BeforeFormat() throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sf.format(new Date()));  // output:2019-08-16 21:46:22

        // String 转 Date
        String str = "2019-10-10 10:10:10";
        System.out.println(sf.parse(str));

        //时间戳的字符串 转 Date
        String tsString = "1556788591462";
        // import java.sql
        Timestamp ts = new Timestamp(Long.parseLong(tsString)); // 时间戳的字符串 转 Date
        System.out.println(new Date(ts.getTime()));
    }

    // 获得昨天此刻的时间
    private void jdk8BeforeGetYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        System.out.println(calendar.getTime());
    }

    // 【JDK 8 时间操作】
    @Test
    public void jdk8After() {
        // jdk8AfterDuration();
        // jdk8AfterGetDatetime();
        jdk8AfterFormat();
        // jdk8AfterGetYesterday();

    }

    // 相隔时间
    private void jdk8AfterDuration() {
        LocalDateTime dt1 = LocalDateTime.now();
        LocalDateTime dt2 = dt1.plusSeconds(60);
        Duration duration = Duration.between(dt1, dt2);
        System.out.println(duration.getSeconds());  // output:60

        LocalDate d1 = LocalDate.now();
        LocalDate d2 = d1.plusDays(2);
        Period period = Period.between(d1, d2);
        System.out.println(period.getDays());   //output:2
    }

    // 获取日期
    private void jdk8AfterGetDatetime() {
        LocalDate localDate = LocalDate.now();
        System.out.println(localDate);

        LocalTime localTime = LocalTime.now();
        System.out.println(localTime);

        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println(localDateTime);

        long milli = Instant.now().toEpochMilli(); // 获取当前时间戳（精确到毫秒）
        long second = Instant.now().getEpochSecond(); // 获取当前时间戳（精确到秒）
        System.out.println(milli);  // output:1565932435792
        System.out.println(second); // output:1565932435
    }

    // 格式化时间
    private void jdk8AfterFormat() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeFormat = dateTimeFormatter.format(LocalDateTime.now());
        System.out.println(timeFormat);  // output:2019-08-16 21:15:43

        String timeFormat2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(timeFormat2);    // output:2019-08-16 21:17:48

        String timeStr = "2019-10-10 06:06:06";
        LocalDateTime dateTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(dateTime);
    }

    // 获取昨天此刻时间
    private void jdk8AfterGetYesterday() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday1 = today.plusDays(-1);
        LocalDateTime yesterday2 = today.minusDays(1);
        System.out.println(yesterday1);
        System.out.println(yesterday2);
    }

    // 获取本月的最后一天
    @Test
    public void getCurMonthLastDay() {
        LocalDateTime today = LocalDateTime.now();
        // 获取本月的最后一天（JDK 8 之前）
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        System.out.println(ca.getTime());
        // 获取本月的最后一天（JDK 8）
        System.out.println(today.with(TemporalAdjusters.lastDayOfMonth()));
    }
}
