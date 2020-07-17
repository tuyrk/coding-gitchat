package com.interview;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 第1-2课：你不知道的基础数据类型和包装类——附面试题
 */
public class Lesson1_2 {
    // 极值查询打印
    @Test
    public void extremeValue() {
        System.out.println(String.format("Byte：%d ~ %d", Byte.MIN_VALUE, Byte.MAX_VALUE));  // -128 ~ 127
        System.out.println(String.format("Int：%d ~ %d", Integer.MIN_VALUE, Integer.MAX_VALUE)); // -2147483648 ~ 2147483647
    }

    @Test
    public void testGeneric() {
        List<Integer> list1 = new ArrayList<>();
        // List<int> list2 = new ArrayList<>(); // 编译器代码报错
    }

    // 高频区缓存范围 -128~127 测试
    @Test
    public void frequencyBuffer() {
        Integer num1 = 127;
        Integer num2 = 127;
        System.out.println("Integer等于127时：" + (num1 == num2)); // true
        Integer num3 = 128;
        Integer num4 = 128;
        System.out.println("Integer等于128时：" + (num3 == num4)); // false
        System.out.println("Integer等于128时 equals：" + num3.equals(num4)); // true
    }

    // Integer 高频区域的最大值演示，修改高频缓存范围
    @Test
    public void changeFrequencyBuffer() {
        Integer i1 = -128;
        Integer i2 = -128;
        System.out.println("值为-128 => " + (i1 == i2)); // true
        Integer i3 = 666;
        Integer i4 = 666;
        System.out.println("值为666 => " + (i3 == i4)); // true
        Integer i5 = 667;
        Integer i6 = 667;
        System.out.println("值为667 => " + (i5 == i6)); // false
    }

    // 面试题示例1,2代码
    @Test
    public void interview1() {
        Integer age1 = 10;
        Integer age2 = 10;
        Integer age3 = 133;
        Integer age4 = 133;
        System.out.println((age1 == age2) + "," + (age3 == age4));

        Double d1 = 10d;
        Double d2 = 10d;
        Double d3 = 133d;
        Double d4 = 133d;
        System.out.println((d1 == d2) + "," + (d3 == d4));
    }

    // int 和 Integer 比较
    @Test
    public void compareInteger() {
        int a = 1000;
        Integer b = new Integer(1000);
        System.out.println(a == b);
        System.out.println(b.equals(a));
    }

    // Integer.MAX_VALUE + 1
    @Test
    public void addMaxValue() {
        final int numMax = Integer.MAX_VALUE;
        System.out.println(numMax + 1);
    }

    // short 类型升级为 int
    @Test
    public void typeChange() {
        Set<Short> set = new HashSet<>();
        for (short i = 0; i < 5; i++) {
            set.add(i);
            set.remove(i - 1);
        }
        System.out.println(set.size());

        short s = 2;
        // s = s + 1; // 编译器报错
        s += 1;

        // float f = 3.4;  // 编译器报错
        float f = 3.4f;
    }

    // new Integer(n) 和 Integer.valueOf(n) 的区别
    @Test
    public void compareInteger2() {
        Integer v1 = new Integer(10);
        Integer v2 = new Integer(10);
        Integer v3 = Integer.valueOf(10);
        Integer v4 = Integer.valueOf(10);

        System.out.println("v3 = " + v3);
        System.out.println("v4 = " + v4);
        // v3 = 20;
        // System.out.println("v3 = " + v3);
        // System.out.println("v4 = " + v4);

        System.out.println(v1 == v2); // false
        System.out.println(v2 == v3); // false
        System.out.println(v3 == v4); // true
    }

    // 精度问题
    @Test
    public void testAccuracy() {
        System.out.println(3 * 0.1);
        System.out.println("1 " + ((1 * 0.1) == 0.1)); // true
        System.out.println("2 " + ((2 * 0.1) == 0.2)); // true
        System.out.println("3 " + ((3 * 0.1) == 0.3)); // false
        System.out.println("4 " + ((4 * 0.1) == 0.4)); // true
        System.out.println("5 " + ((5 * 0.1) == 0.5)); // true
        System.out.println("6 " + ((6 * 0.1) == 0.6)); // false
        System.out.println("7 " + ((7 * 0.1) == 0.7)); // false
        System.out.println("8 " + ((8 * 0.1) == 0.8)); // true
        System.out.println("9 " + ((9 * 0.1) == 0.9)); // true
        System.out.println("10 " + ((10 * 0.1) == 1)); // true
    }
}
