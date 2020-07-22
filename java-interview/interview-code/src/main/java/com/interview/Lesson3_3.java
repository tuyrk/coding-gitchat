package com.interview;

import org.junit.Test;

import java.util.*;

/**
 * 为什么要使用泛型和迭代器 + 面试题
 */
public class Lesson3_3 {
    // 数组遍历
    @Test
    public void traverseArray() {
        String[] arr = {"Java", "Java虚拟机", "Java中文社群"};
        for (int i = 0; i < arr.length; i++) {
            String item = arr[i];
        }
    }

    // 集合遍历
    @Test
    public void traverseList() {
        List<String> list = new ArrayList<String>() {
            {
                add("Java");
                add("Java虚拟机");
                add("Java中文社群");
            }
        };
        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i);
        }
    }

    // Iterator 使用
    @Test
    public void traverseIterator() {
        List<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Java虚拟机");
        list.add("Java中文社群");

        Iterator<String> iter = list.iterator();
        iter.forEachRemaining(System.out::println);

        iter = list.iterator();
        // 遍历
        while (iter.hasNext()) {
            String str = iter.next();
            if ("Java中文社群".equals(str)) {
                iter.remove();
            }
        }
        System.out.println(list);
    }

    // HashMap 的几种循环方式
    @Test
    public void traverseHashMap() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("name", "老王");
        hashMap.put("sex", "你猜");

        // 方式一：entrySet 遍历
        for (Map.Entry<String, String> item : hashMap.entrySet()) {
            System.out.println(item.getKey() + ":" + item.getValue());
        }
        hashMap.forEach((key, value) -> System.out.println(key));

        // 方式二：iterator 遍历
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }

        // 方式三：遍历所有的 key 和 value
        for (Object k : hashMap.keySet()) {
            // 循环所有的 key
            System.out.println(k);
        }
        for (Object v : hashMap.values()) {
            // 循环所有的值
            System.out.println(v);
        }

        // 方式四：通过 key 值遍历
        for (Object k : hashMap.keySet()) {
            System.out.println(k + ":" + hashMap.get(k));
        }
    }

    @Test
    public void test4() {

        List<String> list = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        List list3 = new ArrayList();
        List<?> list4 = new ArrayList<>();
        // list = list2; // error
        list3 = list2;
        list4 = list;
        list4 = list2;
        list4 = list3;

        System.out.println(list.getClass() == list2.getClass());
    }
}
