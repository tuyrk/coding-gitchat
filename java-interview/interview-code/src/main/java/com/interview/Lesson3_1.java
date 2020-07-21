package com.interview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.Test;

import java.util.*;

/**
 * 集合详解之 Collection + 面试题
 */
public class Lesson3_1 {
    // Vector 使用代码
    @Test
    public void vector() {
        Vector vector = new Vector();
        vector.add("dog");
        vector.add("cat");
        vector.remove("cat");
        System.out.println(vector);
    }

    // ArrayList 使用代码
    @Test
    public void arrayList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("dog");
        arrayList.add("cat");
        arrayList.remove("cat");
        System.out.println(arrayList);
    }

    // LinkedList 使用代码
    @Test
    public void linkedList() {
        LinkedList linkedList = new LinkedList();
        // 添加元素
        linkedList.offer("bird");
        linkedList.push("cat");
        linkedList.push("dog");
        // 获取第一个元素
        System.out.println(linkedList.peek());
        // 获取第一个元素，并删除此元素
        System.out.println(linkedList.poll());
        System.out.println(linkedList);
    }

    // HashSet 使用代码
    @Test
    public void hashSet() {
        HashSet hashSet = new HashSet();
        hashSet.add("dog");
        hashSet.add("camel");
        hashSet.add("cat");
        System.out.println(hashSet);
    }

    // TreeSet 使用代码
    @Test
    public void treeSet() {
        TreeSet treeSet = new TreeSet();
        treeSet.add("dog");
        treeSet.add("camel");
        treeSet.add("cat");
        treeSet.add("ant");
        System.out.println(treeSet);
    }

    // LinkedHashSet 使用代码
    @Test
    public void linkedHashSet() {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        linkedHashSet.add("dog");
        linkedHashSet.add("camel");
        linkedHashSet.add("cat");
        linkedHashSet.add("ant");
        System.out.println(linkedHashSet);
    }

    // 集合与数组的互相转换
    @Test
    public void list2Array() {
        List<String> list = new ArrayList<>();
        list.add("cat");
        list.add("dog");
        // 集合转数组
        String[] arr = list.toArray(new String[0]);
        // 数组转集合
        List<String> list2 = Arrays.asList(arr);
        System.out.println(list2);
    }

    // Stack 后进先出演示代码
    @Test
    public void testStack() {
        Stack stack = new Stack();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        for (int i = 0; i < 3; i++) {
            // 移除并返回栈顶元素
            System.out.println(stack.pop());
        }
    }


    // 集合排序（Comparable/Comparator）
    @Test
    public void testSort() {
        DogComp[] dogs = new DogComp[]{
                new DogComp("老旺财", 10),
                new DogComp("小旺财", 3),
                new DogComp("二旺财", 5),
        };
        // Comparable 排序
        Arrays.sort(dogs);
        // Comparator 排序
        /*Arrays.sort(dogs, new DogComparator());
        Arrays.sort(dogs, new Comparator<DogComp>() {
            @Override
            public int compare(DogComp o1, DogComp o2) {
                return o1.getAge() - o2.getAge();
            }
        });
        Arrays.sort(dogs, (o1, o2) -> o1.getAge() - o2.getAge());
        Arrays.sort(dogs, Comparator.comparingInt(DogComp::getAge));*/

        Arrays.asList(dogs).forEach(System.out::println);
    }

    static class DogComparator implements Comparator<DogComp> {
        @Override
        public int compare(DogComp o1, DogComp o2) {
            return o1.getAge() - o2.getAge();
        }
    }

    @Getter
    @ToString
    @AllArgsConstructor
    static class DogComp implements Comparable<DogComp> {
        private String name;
        private int age;

        @Override
        public int compareTo(DogComp o) {
            return age - o.age;
        }
    }

}


