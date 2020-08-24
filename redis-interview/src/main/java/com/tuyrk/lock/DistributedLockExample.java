package com.tuyrk.lock;

import com.tuyrk.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

/**
 * 分布式锁
 */
public class DistributedLockExample {
    private static final String LOCK_KEY = "REDIS_LOCK"; // 锁 key
    private static final String FLAG_ID = "UUID:6379";  // 标识（UUID）
    private static final Integer TIME_OUT = 90;     // 最大超时时间

    public static void main(String[] args) {
        Jedis jedis = JedisUtil.getJedis();
        // 加锁
        boolean lockResult = lock(jedis, LOCK_KEY, FLAG_ID, TIME_OUT);
        // 逻辑业务处理
        if (lockResult) {
            System.out.println("加锁成功");
        } else {
            System.out.println("加锁失败");
        }
        // 手动释放锁
        if (unLock(jedis, LOCK_KEY, FLAG_ID)) {
            System.out.println("锁释放成功");
        } else {
            System.out.println("锁释放成功");
        }
    }

    /**
     * @param jedis       Redis 客户端
     * @param key         锁名称
     * @param flagId      锁标识（锁值），用于标识锁的归属
     * @param secondsTime 最大超时时间
     * @return
     */
    public static boolean lock(Jedis jedis, String key, String flagId, Integer secondsTime) {
        SetParams params = SetParams.setParams();
        params.ex(secondsTime);
        params.nx();
        String res = jedis.set(key, flagId, params);
        return "OK".equals(res);
    }

    /**
     * 释放分布式锁
     *
     * @param jedis   Redis 客户端
     * @param lockKey 锁的 key
     * @param flagId  锁归属标识
     * @return 是否释放成功
     */
    public static boolean unLock(Jedis jedis, String lockKey, String flagId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(flagId));
        // 判断执行结果
        return "1L".equals(result);
    }
}
