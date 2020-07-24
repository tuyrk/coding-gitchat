package com.interview;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池之 ThreadPoolExecutor + 面试题
 */
public class Lesson5_2 {
    // 创建线程池，execute 使用
    @Test
    public void create() throws InterruptedException, ExecutionException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 10, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
        threadPoolExecutor.execute(() -> System.out.println("Hello, Java."));
        threadPoolExecutor.execute(() -> System.out.println("Hello, Java."));
        threadPoolExecutor.execute(() -> System.out.println("Hello, Java."));
        Future<?> submit = threadPoolExecutor.submit(() -> System.out.println("Hello, Java."));
        System.out.println(submit.isDone());
        System.out.println(submit.get());
        System.out.println(submit.isDone());
        TimeUnit.SECONDS.sleep(20);
        System.out.println(threadPoolExecutor.getPoolSize());
        System.out.println(threadPoolExecutor.getCorePoolSize());
        System.out.println(threadPoolExecutor.getLargestPoolSize());
        System.out.println(threadPoolExecutor.getMaximumPoolSize());
    }

    // 创建线程池,全参数。ThreadPoolExecutor 七个参数的使用示例
    @Test
    public void createAll() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1,
                1, 10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2), new MyThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        threadPool.allowCoreThreadTimeOut(true);
        for (int i = 0; i < 10; i++) {
            threadPool.execute(() -> {
                System.out.println(Thread.currentThread().getName());
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println(threadPool.getActiveCount());
        System.out.println(threadPool.getTaskCount());
        System.out.println(threadPool.getCompletedTaskCount());
        BlockingQueue<Runnable> queue = threadPool.getQueue();
    }

    // execute() VS submit()
    @Test
    public void executeVsSubmit() throws InterruptedException, ExecutionException {
        // 创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 10, 10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
        // execute 使用
        threadPoolExecutor.execute(() -> System.out.println("Hello, Java."));
        // submit 使用
        Future<String> future = threadPoolExecutor.submit(() -> {
            System.out.println("Hello, 老王.");
            return "Success";
        });
        System.out.println(future.get());
    }

    // shutdown
    @Test
    public void shutdown() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 10,
                10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
        threadPoolExecutor.execute(() -> {
            for (int i = 0; i < 2; i++) {
                System.out.println("I'm " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        threadPoolExecutor.shutdown();
        threadPoolExecutor.execute(() -> System.out.println("I'm Java."));
    }

    // shutdownNow
    @Test
    public void shutdownNow() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1,
                10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
        threadPoolExecutor.execute(() -> {
            for (int i = 0; i < 1000; i++) {
                System.out.println("I'm " + i);
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        });
        threadPoolExecutor.execute(() -> System.out.println("shutdown now"));

        System.out.println(threadPoolExecutor.isShutdown());
        System.out.println(threadPoolExecutor.isTerminated());
        System.out.println(threadPoolExecutor.isTerminating());
        System.out.println("====");
        List<Runnable> runnableList = threadPoolExecutor.shutdownNow();
        System.out.println(threadPoolExecutor.isShutdown());
        System.out.println(threadPoolExecutor.isTerminated());
        System.out.println(threadPoolExecutor.isTerminating());

        System.out.println(runnableList.size());
        runnableList.forEach(System.out::println);
    }

    @Test
    public void shutdownNow2() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 10, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        threadPoolExecutor.execute(() -> {
            for (int i = 0; i < 2; i++) {
                System.out.println("I：" + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadPoolExecutor.shutdownNow();
        System.out.println("Java");
    }

    @Test
    public void test() {
        //org.apache.commons.lang3.concurrent.BasicThreadFactory
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());

        // 创建线程工厂类
        ThreadFactory namedThreadFactory = new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build();

        //Common Thread Pool
        ExecutorService pool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        pool.execute(() -> System.out.println(Thread.currentThread().getName()));
        pool.shutdown();//gracefully shutdown
    }
}

class MyThreadFactory implements ThreadFactory {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        String threadName = "MyThread" + count.addAndGet(1);
        t.setName(threadName);
        return t;
    }
}

class MyRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // 记录异常、报警处理等
        System.out.println("Error Message.");
    }
}
