package com.interview;

import org.junit.Test;


public class Lesson1_3 {
    // String 引用对比
    @Test
    public void testEquals() {
        String s1 = "laowang";
        String s2 = s1;
        String s3 = new String(s1);
        System.out.println(s1 == s2); // true
        System.out.println(s1 == s3); // false
    }

    // java编译对String的优化
    @Test
    public void testEqualsUpgrade() {
        String as1 = "hi," + "lao" + "wang";
        String as2 = "hi,";
        as2 += "lao";
        as2 += "wang";
        String as3 = "hi,laowang";

        System.out.println(as1 == as2); // false
        System.out.println(as1 == as3); // true
        System.out.println(as2 == as3); // false
    }

    class Animal {
        public void move() {
            System.out.println("动物可以移动");
        }
    }

    class Dog extends Animal {
        @Override
        public void move() {
            System.out.println("狗可以跑和走");
        }

        public void bark() {
            System.out.println("狗可以吠叫");
        }
    }

    @Test
    public void testNot() {
        Animal a = new Animal();
        Animal b = new Dog();
        a.move();
        b.move();
        // b.bark();
    }

    // String参数传递
    @Test
    public void testParamString() {
        String str = new String("laowang");
        change(str);
        System.out.println(str); // laowang
    }

    // StringBuffer参数传递
    @Test
    public void testParamStringBuffer() {
        StringBuffer sb = new StringBuffer("hi,");
        changeBuffer(sb);
        System.out.println(sb); // hi,laowang
    }

    // substring截取
    @Test
    public void testSubstring() {
        String subStr = "ABCDEF";
        System.out.println(subStr.substring(2)); // CDEF
        System.out.println(subStr.substring(2, 4)); // CD（包含开始下标不包含结束下标）
    }

    // String.format
    @Test
    public void format() {
        String sf = String.format("我叫%s，今年%d岁，喜欢%s", "老王", 30, "读书");
        System.out.println(sf);
    }

    // String equals
    @Test
    public void equals() {
        String es1 = "hi," + "lao" + "wang";
        String es2 = "hi,";
        es2 += "lao";
        es2 += "wang";
        String es3 = "hi,laowang";
        System.out.println(es1.equals(es2)); // true
        System.out.println(es1.equals(es3)); // true
        System.out.println(es2.equals(es3)); // true

        System.out.println(es1 == es2); // false
        System.out.println(es1 == es3); // true
        System.out.println(es2 == es3); // false
    }

    // String 大小写比较
    @Test
    public void equalsIgnoreCase() {
        String eis1 = "Hi,laowang";
        String eis2 = "hi,laowang";
        System.out.println(eis1.equals(eis2)); // false
        System.out.println(eis1.equalsIgnoreCase(eis2)); // true
    }

    // StringBuilder 用法
    @Test
    public void testStringBuilder() {
        StringBuilder stringBuilder = new StringBuilder("lao");
        // 添加字符串到尾部
        stringBuilder.append("wang"); // laowang
        // 插入字符串到到当前字符串下标的位置
        stringBuilder.insert(0, "hi,"); // hi,laowang
        // 修改字符中某个下标的值
        stringBuilder.setCharAt(0, 'H'); // Hi,laowang
        System.out.println(stringBuilder);
    }

    // String intern 用法
    @Test
    public void intern() {
        String str1 = "hi";
        String str2 = new String("hi");
        String str3 = new String("hi").intern();
        System.out.println(str1 == str2); // false
        System.out.println(str1 == str3); // true
    }

    public static void change(String s) {
        s = "xiaowang";
    }

    public static void changeBuffer(StringBuffer sb) {
        sb.append("laowang");
    }
}
