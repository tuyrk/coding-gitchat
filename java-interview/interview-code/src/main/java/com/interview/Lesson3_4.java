package com.interview;

import org.junit.Test;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 数据结构之队列的使用 + 面试题
 */
public class Lesson3_4 {
    // DelayQueue 延迟队列使用
    @Test
    public void delayQueue() throws InterruptedException {
        DelayQueue delayQueue = new DelayQueue();
        delayQueue.put(new DelayElement(1000));
        delayQueue.put(new DelayElement(3000));
        delayQueue.put(new DelayElement(5000));
        System.out.println("开始时间：" + DateFormat.getDateTimeInstance().format(new Date()));
        while (!delayQueue.isEmpty()) {
            System.out.println(delayQueue.take());
        }
        System.out.println("结束时间：" + DateFormat.getDateTimeInstance().format(new Date()));
    }

    static class DelayElement implements Delayed {
        // 延迟截止时间（单面：毫秒）
        long delayTime = System.currentTimeMillis();

        public DelayElement(long delayTime) {
            this.delayTime = (this.delayTime + delayTime);
        }

        @Override
        // 获取剩余时间
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        // 队列里元素的排序依据
        public int compareTo(Delayed o) {
            if (this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
                return 1;
            } else if (this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return DateFormat.getDateTimeInstance().format(new Date(delayTime));
        }
    }

    // ConcurrentLinkedQueue 使用
    @Test
    public void concurrentLinkedQueue() {
        ConcurrentLinkedQueue concurrentLinkedQueue = new ConcurrentLinkedQueue();
        concurrentLinkedQueue.add("Dog");
        concurrentLinkedQueue.add("Cat");
        while (!concurrentLinkedQueue.isEmpty()) {
            System.out.println(concurrentLinkedQueue.poll());
        }
    }

    // PriorityQueue 使用
    @Test
    public void priorityQueue() {
        Queue<Integer> priorityQueue = new PriorityQueue((Comparator<Integer>) (o1, o2) -> o2 - o1);
        priorityQueue.add(3);
        priorityQueue.add(1);
        priorityQueue.add(2);
        while (!priorityQueue.isEmpty()) {
            Integer i = priorityQueue.poll();
            System.out.println(i);
        }
    }

    // LinkedList 使用
    @Test
    public void linkedList() {
        Queue<String> linkedList = new LinkedList<>();
        linkedList.add("Dog");
        linkedList.add("Camel");
        linkedList.add("Cat");
        while (!linkedList.isEmpty()) {
            System.out.println(linkedList.poll());
        }
    }
}


