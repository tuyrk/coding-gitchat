package com.interview;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 线程池之 Executors + 面试题
 */
public class Lesson5_3 {
    @Test
    public void ScheduledThreadPoolExecutor() {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        ScheduledExecutorService scheduledExecutorService = scheduledThreadPoolExecutor;
        System.out.println(scheduledThreadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
    }

    // newFixedThreadPool 使用
    @Test
    public void newFixedThreadPool() throws InterruptedException {
        class ThreadFactoryImpl implements ThreadFactory {
            private AtomicInteger increase = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Thread - " + increase.addAndGet(1));
                return thread;
            }
        }
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2, new ThreadFactoryImpl());
        IntStream.rangeClosed(1, 3).forEachOrdered(e -> {
            fixedThreadPool.execute(() -> {
                System.out.println("CurrentTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
        });
        TimeUnit.SECONDS.sleep(5);
    }

    // newCachedThreadPool 使用
    @Test
    public void newCachedThreadPool() throws InterruptedException {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        IntStream.rangeClosed(1, 10).forEachOrdered(e -> {
            cachedThreadPool.execute(() -> {
                System.out.println("CurrentTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
        });
        TimeUnit.SECONDS.sleep(5);
    }

    // newSingleThreadExecutor 使用
    @Test
    public void newSingleThreadExecutor() throws InterruptedException {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        IntStream.rangeClosed(1, 3).forEachOrdered(e -> {
            singleThreadExecutor.execute(() -> {
                System.out.println("CurrentTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
        });
        TimeUnit.SECONDS.sleep(5);
    }

    // newScheduledThreadPool 使用
    @Test
    public void newScheduledThreadPool() throws InterruptedException {
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
        System.out.println("StartTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 两秒后执行
        scheduledThreadPool.schedule(() -> System.out.println("schedule - CurrentTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
                2L, TimeUnit.SECONDS);

        // 两秒后开始执行任务，每隔三秒执行一次。若执行时间大于3秒，则执行完就执行下一次任务
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            System.out.println("scheduleAtFixedRate - CurrentTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 2L, 3L, TimeUnit.SECONDS);

        // 两秒后开始执行任务，任务执行完成后等待三秒，再执行下一次任务。
        scheduledThreadPool.scheduleWithFixedDelay(() -> {
            System.out.println("scheduleWithFixedDelay - CurrentTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 2L, 3L, TimeUnit.SECONDS);
        TimeUnit.MINUTES.sleep(20);
    }

    // newSingleThreadScheduledExecutor 使用
    @Test
    public void newSingleThreadScheduledExecutor() throws InterruptedException, ExecutionException {
        System.out.println("StartTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ScheduledExecutorService singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<Boolean> schedule = singleThreadScheduledExecutor.schedule(() -> {
            System.out.println("CurrentTime - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return true;
        }, 2L, TimeUnit.SECONDS);
        Boolean result = schedule.get();
        System.out.println(result);
        TimeUnit.SECONDS.sleep(5);
    }

    // newWorkStealingPool 使用
    @Test
    public void newWorkStealingPool() throws InterruptedException {
        ExecutorService workStealingPool = Executors.newWorkStealingPool();
        IntStream.rangeClosed(1, 20).forEachOrdered(e -> {
            workStealingPool.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.printf("CurrentTime %d - %s\n", e, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            });
        });
        TimeUnit.SECONDS.sleep(60);
    }
}
