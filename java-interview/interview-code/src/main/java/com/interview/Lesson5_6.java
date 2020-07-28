package com.interview;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Java 并发包中的高级同步工具 + 面试题
 */
public class Lesson5_6 {

    // CountDownLatch 使用
    @Test
    public void countdownLatch() throws InterruptedException {
        CountDownLatch hospitalLatch = new CountDownLatch(1); // 医院闭锁
        CountDownLatch patientLatch = new CountDownLatch(5); // 患者闭锁
        System.out.println("患者排队");
        ExecutorService executorService = Executors.newCachedThreadPool();
        IntStream.rangeClosed(1, 5).forEachOrdered(e -> {
            executorService.execute(() -> {
                try {
                    hospitalLatch.await();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("体检：" + e);
                patientLatch.countDown();
            });
        });
        System.out.println("医生上班");
        hospitalLatch.countDown();
        patientLatch.await();
        System.out.println("医生下班");
        executorService.shutdown();
    }

    // CyclicBarrier使用
    @Test
    public void cyclicBarrier() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(4, () -> System.out.println("发车了"));

        // 配合演示 CyclicBarrier
        class CyclicWorker implements Runnable {
            private CyclicBarrier cyclicBarrier;

            CyclicWorker(CyclicBarrier cyclicBarrier) {
                this.cyclicBarrier = cyclicBarrier;
            }

            @Override
            public void run() {
                IntStream.rangeClosed(1, 2).forEachOrdered(i -> {
                    System.out.println(Thread.currentThread().getName() + "乘客：" + i);
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        IntStream.rangeClosed(1, 4).forEachOrdered(e -> {
            new Thread(new CyclicWorker(cyclicBarrier)).start();
        });
        TimeUnit.SECONDS.sleep(5);
    }

    // Semaphore使用
    @Test
    public void semaphore() throws InterruptedException {
        Semaphore semaphore = new Semaphore(2);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 50,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);
        IntStream.rangeClosed(1, 7).forEachOrdered(i -> executor.execute(() -> {
            try {
                // 堵塞获取许可
                semaphore.acquire();
                System.out.println("Thread:" + Thread.currentThread().getName() + " 时间:" + LocalDateTime.now());
                TimeUnit.SECONDS.sleep(2);
                // 释放许可
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        TimeUnit.SECONDS.sleep(20);
    }

    // Phaser使用
    @Test
    public void phaser() throws InterruptedException {
        int workerCount = 5;

        Phaser phaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                switch (phase) {
                    case 0:
                        System.out.println("==== 集合完毕发车 ====");
                        return false;
                    case 1:
                        System.out.println("==== 景点1集合完毕，发车去下一个景点 ====");
                        return false;
                    case 2:
                        System.out.println("==== 景点2集合完毕，发车回家 ====");
                        return false;
                    default:
                        return true;
                }
            }
        };
        for (int i = 0; i < workerCount; i++) {
            // 注册 phaser 等待的线程数，执行一次等待线程数+1
            phaser.register();
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(workerCount, workerCount,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);
        for (int i = 0; i < workerCount; i++) {
            // 执行任务
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " | 到达");
                phaser.arriveAndAwaitAdvance(); // 集合完毕发车
                try {
                    Thread.sleep(new Random().nextInt(5) * 1000);
                    System.out.println(Thread.currentThread().getName() + " | 到达");
                    phaser.arriveAndAwaitAdvance(); // 景点1集合完毕发车

                    Thread.sleep(new Random().nextInt(5) * 1000);
                    System.out.println(Thread.currentThread().getName() + " | 到达");
                    phaser.arriveAndAwaitAdvance(); // 景点2集合完毕发车
                } catch (InterruptedException ignored) {
                }
            });
        }

        TimeUnit.SECONDS.sleep(20);
    }

    @Test
    public void semaphore2() {
        Semaphore semaphore = new Semaphore(2);
        ThreadPoolExecutor semaphoreThread = new ThreadPoolExecutor(10, 50,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);
        System.out.println(semaphore.availablePermits());
        long start = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            semaphoreThread.execute(() -> {
                try {
                    semaphore.release();
                    System.out.println("Hello");
                    TimeUnit.SECONDS.sleep(2);
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println(semaphore.availablePermits());
        semaphoreThread.shutdown();
        while (true) {
            if (semaphoreThread.isTerminated()) {
                long end = System.currentTimeMillis();
                System.out.println(end - start);
                break;
            }
        }
    }

    @Test
    public void cyclicBarrier2() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(4, () -> System.out.println("发车了"));
        IntStream.rangeClosed(1, 4).forEachOrdered(i -> {
            new Thread(() -> {
                IntStream.rangeClosed(1, 2).forEachOrdered(j -> {
                    try {
                        System.out.println("乘客：" + j);
                        cyclicBarrier.await();
                        System.out.println("乘客 II：" + j);
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                });

            }).start();
        });
        TimeUnit.SECONDS.sleep(5);
    }
}
