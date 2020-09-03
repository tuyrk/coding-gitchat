package com.lynn.demo;

import com.lynn.demo.lock.RedisLocker;
import com.lynn.demo.zk.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private DistributedLockHandler distributedLockHandler;

    @RequestMapping("index1")
    public String index1() throws Exception {
        Lock lock = new Lock("lynn", "min");
        if (distributedLockHandler.tryLock(lock)) {
            // 为了演示锁的效果，这里睡眠5000毫秒
            System.out.println("执行方法");
            Thread.sleep(5000);
            distributedLockHandler.releaseLock(lock);
        }
        return "hello world!";
    }

    @Autowired
    private RedisLocker redisLocker;

    @RequestMapping("index2")
    public String index2() throws Exception {
        redisLocker.lock("test", () -> {
            System.out.println("执行方法！");
            Thread.sleep(5000);
            return null;
        });
        return "hello world!";
    }

    @RequestMapping("index3")
    public String index3() throws Exception {
        DistributedLock lock = new DistributedLock("localhost:2181", "lock");
        lock.lock();
        //共享资源
        if (lock != null) {
            System.out.println("执行方法");
            Thread.sleep(5000);
            lock.unlock();
        }
        return "hello world!";
    }
}
