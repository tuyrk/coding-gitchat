package com.interview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 为什么需要线程 + 面试题
 */
@Slf4j
public class Lesson5_1 {
    // 创建方式一：继承 Thread 类
    @Test
    public void thread() {
        MyThread thread = new MyThread();
        thread.start();
    }

    // 创建方式二：实现 Runnable 接口
    @Test
    public void runnable() {
        MyRunnable runnable = new MyRunnable();
        Thread thread = new Thread(runnable);
        thread.start();
    }

    // 创建方式三：实现 Callable 接口
    @Test
    public void callable() throws ExecutionException, InterruptedException {
        MyCallable callable = new MyCallable();
        FutureTask<String> task = new FutureTask<>(callable);
        new Thread(task).start();
        System.out.println(task.get());
    }

    // JDK 8 lambda Thread
    @Test
    public void lambda() throws ExecutionException, InterruptedException {
        new Thread(() -> System.out.println("Lambda Of Thread.")).start();
    }

    // 线程休眠
    @Test
    public void waits() throws InterruptedException {
        System.out.println(LocalDateTime.now());
        Object lock = new Object();
        Thread lockThread = new Thread(() -> {
            synchronized (lock) {
                try {
                    // 1 秒钟之后自动唤醒
                    lock.wait(1000);
                    System.out.println(LocalDateTime.now());
                } catch (InterruptedException ignored) {
                }
            }
        });
        lockThread.start();
        lockThread.join();
    }

    // join 使用
    @Test
    public void join() throws InterruptedException {
        Thread joinThread = new Thread(() -> {
            try {
                System.out.println("执行前");
                Thread.sleep(1000);
                System.out.println("执行后");
            } catch (InterruptedException ignored) {
            }
        });
        joinThread.start();
        joinThread.join();
        System.out.println("主程序");
    }

    // yield 使用
    @Test
    public void yield() throws InterruptedException {
        new Thread() {
            @Override
            public void run() {
                for (int i = 1; i < 10; i++) {
                    if (i == 5) {
                        // 让同优先级的线程有执行的机会，但不能保证自己会从正在运行的状态迅速转换到可运行的状态
                        Thread.yield();
                    }
                }
            }
        }.start();
    }

    // interrupt 使用
    @Test
    public void interrupt() throws InterruptedException {
        Thread interruptThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    System.out.println("i：" + i);
                    if (this.isInterrupted()) {
                        break;
                    }
                }
            }
        };
        interruptThread.start();
        Thread.sleep(10);
        interruptThread.interrupt();
    }

    // 设置线程优先级
    @Test
    public void priority() {
        Thread priorityThread = new Thread(() -> System.out.println("Java"));
        priorityThread.setPriority(10);
        System.out.println(priorityThread.getPriority());
        priorityThread.start();
    }

    // 死锁演示
    @Test
    public void lock() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        // 线程1拥有对象1，想要等待获取对象2
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
        // 线程2拥有对象2，想要等待获取对象1
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

    class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("Thread");
        }
    }

    class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("Runnable");
        }
    }

    class MyCallable implements Callable {
        @Override
        public Object call() throws Exception {
            System.out.println("Callable");
            return "Success";
        }
    }
}




