package com.tuyrk.lock;

import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RedissonLock {
    private final static String LOCKER_PREFIX = "lock:";
    private static RedissonClient redissonClient = null;

    public RedissonLock() {
        Config config = new Config();
        config.useSingleServer().setTimeout(1000000).setAddress("redis://127.0.0.1:6379");
        redissonClient = Redisson.create(config);
    }

    public <T> T lock(String resourceName, AquiredLockWorker<T> worker) throws Exception {
        return lock(resourceName, worker, 100);
    }

    public <T> T lock(String resourceName, AquiredLockWorker<T> worker, int lockTime) throws Exception {
        RLock lock = redissonClient.getLock(LOCKER_PREFIX + resourceName);
        // Wait for 100 seconds seconds and automatically unlock it after lockTime seconds
        boolean success = lock.tryLock(100, lockTime, TimeUnit.SECONDS);
        if (success) {
            try {
                return worker.invokeAfterLockAquire();
            } finally {
                lock.unlock();
            }
        }
        throw new RuntimeException();
    }

    @Test
    public void main() throws Exception {
        lock("test", () -> {
            System.out.println("执行方法！");
            TimeUnit.MINUTES.sleep(5);
            return null;
        });
    }
}
