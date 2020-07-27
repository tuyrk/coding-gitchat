package com.interview;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程安全之 synchronized 和 ReentrantLock + 面试题
 */
public class Lesson5_5 {
    // 非线程安全代码演示
    @Test
    public void synchronizeds() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            /*addNumber();*/
            synchronized (this) {
                addNumber();
            }
        });
        Thread thread2 = new Thread(() -> {
            /*addNumber();*/
            synchronized (this) {
                addNumber();
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("number：" + number);
    }

    // synchronized 使用
    @Test
    public void synchronizedObj() throws InterruptedException {
        Object syn = new Object();
        Thread sThread = new Thread(() -> {
            synchronized (syn) {
                addNumber();
            }
        });
        Thread sThread2 = new Thread(() -> {
            synchronized (syn) {
                addNumber();
            }
        });
        sThread.start();
        sThread2.start();
        sThread.join();
        sThread2.join();
        System.out.println("number：" + number);
    }

    // ReentrantLock 基本使用
    @Test
    public void reentrantLock() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Thread thread1 = new Thread(() -> {
            lock.lock();
            try {
                addNumber();
            } finally {
                lock.unlock();
            }

        });
        Thread thread2 = new Thread(() -> {
            lock.lock();
            try {
                addNumber();
            } finally {
                lock.unlock();
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("number：" + number);
    }

    // ReentrantLock tryLock 基本使用
    @Test
    public void reentrantTryLock() throws InterruptedException {
        ReentrantLock tryLock = new ReentrantLock();
        // 线程一
        new Thread(() -> {
            tryLock.lock();
            try {
                System.out.println(tryLock.getHoldCount());
                System.out.println(tryLock.getQueueLength());
                System.out.println(LocalDateTime.now());
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                tryLock.unlock();
            }

        }).start();
        // 线程二
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                System.out.println("1" + tryLock.tryLock());

                System.out.println(tryLock.getHoldCount());
                System.out.println(tryLock.getQueueLength());

                TimeUnit.SECONDS.sleep(2);
                System.out.println("2" + tryLock.tryLock());
                System.out.println(LocalDateTime.now());
                System.out.println(tryLock.tryLock(3, TimeUnit.SECONDS));
                System.out.println(LocalDateTime.now());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        TimeUnit.SECONDS.sleep(5);
    }

    // ReentrantLock lockInterruptibly() vs lock()
    @Test
    public void lockInterruptibly() throws InterruptedException {
        Lock interruptLock = new ReentrantLock();
        interruptLock.lock();
        Thread thread = new Thread(() -> {
            try {
                // interruptLock.lock();
                interruptLock.lockInterruptibly();  // java.lang.InterruptedException
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        TimeUnit.SECONDS.sleep(1);
        thread.interrupt();
        TimeUnit.SECONDS.sleep(3);
        System.out.println("Over");
    }

    @Test
    public void test() {
        ReentrantLock noFairLock = new ReentrantLock();
        ReentrantLock fairLock = new ReentrantLock(true);
    }

    public static int number = 0;

    public /*synchronized*/ static void addNumber() {
        for (int i = 0; i < 10000; i++) {
            ++number;
            /*synchronized (Lesson5_5.class) {
                ++number;
            }*/
        }
    }
}
