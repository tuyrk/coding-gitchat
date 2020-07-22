package com.interview;

import lombok.*;
import org.junit.Test;

import java.util.*;

/**
 * 集合详解之 Map + 面试题
 */
public class Lesson3_2 {
    @Test
    public void crud() {
        Map hashMap = new HashMap();
        // 增加元素
        hashMap.put("name", "老王");
        hashMap.put(null, null);
        hashMap.put("age", "30");
        hashMap.put("sex", "你猜");
        // 删除元素
        hashMap.remove(null);
        // 查找单个元素
        System.out.println(hashMap.get("age"));

        // 循环所有的 key
        for (Object k : hashMap.keySet()) {
            System.out.println(k);
        }
        // 循环所有的值
        for (Object v : hashMap.values()) {
            System.out.println(v);
        }
        for (Object o : hashMap.entrySet()) {
            System.out.println(o);
        }
        hashMap.entrySet().forEach(System.out::println);
    }

    // TreeMap 自定义排序
    @Test
    public void sortTreeMap() {
        // --------------------------  ----------------------------
        TreeMap<String, String> treeMap = new TreeMap(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s2.compareTo(s1); // 倒序
            }
        });
        treeMap.put("dog", "dog");
        treeMap.put("camel", "camel");
        treeMap.put("cat", "cat");
        treeMap.put("ant", "ant");
        treeMap.entrySet().forEach(System.out::println);
    }

    @Test
    public void reverseTreeMap() {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("dog", "dog");
        treeMap.put("camel", "camel");
        treeMap.put("cat", "cat");
        treeMap.put("ant", "ant");
        // map.entrySet() 转成 List
        List<Map.Entry<String, String>> list = new ArrayList<>(treeMap.entrySet());
        // 通过比较器实现比较排序
        list.sort(new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> m1, Map.Entry<String, String> m2) {
                return m2.getValue().compareTo(m1.getValue());
            }
        });
        // 打印结果
        for (Map.Entry<String, String> item : list) {
            System.out.println(item.getKey() + ":" + item.getValue());
        }
    }

    // LinkedHashMap 使用
    @Test
    public void linkedHashMap() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("dog", "dog");
        linkedHashMap.put("camel", "camel");
        linkedHashMap.put("cat", "cat");
        linkedHashMap.put("ant", "ant");
        System.out.println(linkedHashMap);
    }

    // hashCode 和 equals 测试
    @Test
    public void testHashCodeEquals() {
        HashMap<Person, Integer> map = new HashMap<>();
        Person person = new Person(18);
        map.put(person, 1);
        System.out.println(map.get(new Person(18)));
    }

    // hashCode 和 equals 测试
    @Test
    public void testHashCodeEquals2() {
        Person p1 = new Person(18);
        Person p2 = new Person(18);
        System.out.println(p1.equals(p2));
        System.out.println(p1.hashCode() + " : " + p2.hashCode());
    }
}

@Data
@AllArgsConstructor
class Person {
    private Integer age;

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Person)) {
            return false;
        } else {
            return this.getAge().equals(((Person) o).getAge());
        }
    }

    /*@Override
    public int hashCode() {
        return age.hashCode();
    }*/
}
