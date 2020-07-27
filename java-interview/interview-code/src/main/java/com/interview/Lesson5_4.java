package com.interview;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * ThreadLocal
 */
public class Lesson5_4 {
    // ThreadLocal 使用
    @Test
    public void use() {
        ThreadLocal<List<String>> threadLocal = new ThreadLocal<>();
        threadLocal.set(Arrays.asList("老王", "Java面试题"));
        List<String> list = threadLocal.get();
        System.out.println(list);
        threadLocal.remove();
        System.out.println(threadLocal.get());
    }

    // InheritableThreadLocal 使用。ThreadLocal 数据共享
    @Test
    public void inheritable() {
        ThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        inheritableThreadLocal.set("老王");
        new Thread(() -> {
            System.out.println(inheritableThreadLocal.get()); // 老王
            inheritableThreadLocal.set("老李");
        }).start();
        new Thread(() -> {
            System.out.println(inheritableThreadLocal.get()); // 老王
        }).start();
        System.out.println(inheritableThreadLocal.get()); // 老王
    }

    // ThreadLocal 模拟内存溢出
    @Test
    public void oom() throws InterruptedException {
        Integer THREAD_MAX = 100;
        ThreadLocal<List<String>> threadLocal = new ThreadLocal<>();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_MAX, THREAD_MAX,
                0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);
        for (int i = 0; i < THREAD_MAX; i++) {
            executor.execute(() -> {
                threadLocal.set(getList());
                System.out.println(Thread.currentThread().getName());
                // 移除对象
                // threadLocal.remove(); // 不注释此行内存溢出
            });
            TimeUnit.SECONDS.sleep(1);
        }
        executor.shutdown();
    }

    // InheritableThreadLocal and ThreadLocal 值比较
    @Test
    public void vs() {
        ThreadLocal threadLocal1 = new InheritableThreadLocal();
        threadLocal1.set("老王");
        ThreadLocal threadLocal2 = new ThreadLocal();
        threadLocal2.set("老王");
        new Thread(() -> {
            System.out.println(threadLocal1.get());
            System.out.println(threadLocal2.get());
            System.out.println(threadLocal1.get().equals(threadLocal2.get()));
        }).start();
    }

    private List<String> getList() {
        Integer MOCK_MAX = 10000;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < MOCK_MAX; i++) {
            list.add("Version：JDK 8");
            list.add("ThreadLocal");
            list.add("Author：老王");
            list.add("DateTime：" + LocalDateTime.now());
            list.add("Test：ThreadLocal OOM");
        }
        return list;
    }
}
