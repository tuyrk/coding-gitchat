package com.tuyrk.lock;

/**
 * 单机锁死锁的示例
 */
public class SingleDeadlock {
    public static void main(String[] args) {
        Object obj1 = new Object();
        Object obj2 = new Object();
        // 线程 1 拥有对象 1，想要等待获取对象 2
        new Thread(() -> {
            synchronized (obj1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (obj2) {
                    System.out.println(Thread.currentThread().getName());
                }
            }
        }).start();
        // 线程 2 拥有对象 2，想要等待获取对象 1
        new Thread(() -> {
            synchronized (obj2) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (obj1) {
                    System.out.println(Thread.currentThread().getName());
                }
            }
        }).start();
    }
}
