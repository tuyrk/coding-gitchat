package com.interview;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Java 中的各种锁和 CAS + 面试题
 */
public class Lesson5_7 {
    // 共享锁演示
    @Test
    public void readWriteLock() throws InterruptedException {
        final MyReadWriteLock rwLock = new MyReadWriteLock();
        // 创建读锁 r1 和 r2
        Thread r1 = new Thread(rwLock::read, "r1");
        Thread r2 = new Thread(rwLock::read, "r2");
        r1.start();
        r2.start();
        // 等待同时读取线程执行完成
        r1.join();
        r2.join();

        // 开启写锁的操作
        new Thread(rwLock::write, "w1").start();
        new Thread(rwLock::write, "w2").start();
        TimeUnit.SECONDS.sleep(10);
    }

    // AtomicStampedReference（解决 ABA 问题）使用演示
    @Test
    public void aba() {
        String name = "老王";
        String newName = "Java";
        AtomicStampedReference<String> as = new AtomicStampedReference<>(name, 1);
        System.out.println("值：" + as.getReference() + " | Stamp：" + as.getStamp());
        as.compareAndSet(name, newName, as.getStamp(), as.getStamp() + 1);
        System.out.println("值：" + as.getReference() + " | Stamp：" + as.getStamp());
    }

    static class MyReadWriteLock {
        ReadWriteLock lock = new ReentrantReadWriteLock();

        public void read() {
            try {
                lock.readLock().lock();
                System.out.println("读操作，进入 | 线程：" + Thread.currentThread().getName());
                Thread.sleep(3000);
                System.out.println("读操作，退出 | 线程：" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.readLock().unlock();
            }
        }

        public void write() {
            try {
                lock.writeLock().lock();
                System.out.println("写操作，进入 | 线程：" + Thread.currentThread().getName());
                Thread.sleep(3000);
                System.out.println("写操作，退出 | 线程：" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

}


