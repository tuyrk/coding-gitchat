package com.interview;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Calendar;

/**
 * 第1-1课：Java程序是如何执行的？
 */
public class Lesson1_1 {
    // 获取明天此刻时间
    @Test
    public void getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        System.out.println(calendar.getTime());

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime tomorrow1 = today.plusDays(1);
        LocalDateTime tomorrow2 = today.minusDays(-1);
        System.out.println(tomorrow1);
        System.out.println(tomorrow2);
    }

    // 跳出多重嵌套循环
    @Test
    public void breakFor1() {
        myfor:
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                System.out.println("J:" + j);
                if (j == 10) {
                    // 跳出多重循环
                    break myfor;
                }
            }
        }
    }

    @Test
    public void breakFor2() {
        boolean flag = true;
        for (int i = 0; i < 100 && flag; i++) {
            for (int j = 0; j < 100; j++) {
                System.out.println("J:" + j);
                if (j == 10) {
                    // 跳出多重循环
                    flag = false;
                    break;
                }
            }
        }
    }
}
