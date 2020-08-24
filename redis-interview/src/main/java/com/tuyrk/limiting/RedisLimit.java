package com.tuyrk.limiting;

import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.stream.IntStream;

/**
 * Redis 如何实现限流功能？
 */
public class RedisLimit {
    // Redis 操作客户端
    private static Jedis JEDIS = new Jedis("127.0.0.1", 6379);

    @Test
    public void main() throws InterruptedException {
        IntStream.rangeClosed(1, 15).forEachOrdered(this::task);
        Thread.sleep(4000); // 休眠 4s
        // 超过最大执行时间之后，再从发起请求
        task(16);
    }

    /**
     * 执行任务
     */
    private void task(int idx) {
        boolean res = isPeriodLimiting("java", 3, 10);
        if (res) {
            System.out.println(idx + "休眠后，正常执行请求");
        } else {
            System.out.println(idx + "休眠后，被限流");
        }
    }

    /**
     * 限流方法（滑动时间算法）
     *
     * @param key      限流标识
     * @param period   限流时间范围（单位：秒）
     * @param maxCount 最大运行访问次数
     * @return 是否执行限流
     */
    private static boolean isPeriodLimiting(String key, int period, int maxCount) {
        long nowTs = System.currentTimeMillis(); // 当前时间戳
        // 删除非时间段内的请求数据（清除老访问数据，比如 period=60 时，标识清除 60s 以前的请求记录）
        JEDIS.zremrangeByScore(key, 0, nowTs - period * 1000);
        long currCount = JEDIS.zcard(key); // 当前请求次数
        // 超过最大请求次数，执行限流
        if (currCount >= maxCount) {
            return false;
        }
        // 未达到最大请求数，正常执行业务
        JEDIS.zadd(key, nowTs, "" + nowTs); // 请求记录 +1
        return true;
    }
}
/*
deb http://archive.ubuntu.com/ubuntu/ bionic main restricted deb http://archive.ubuntu.com/ubuntu/ bionic-updates main restricted deb http://archive.ubuntu.com/ubuntu/ bionic universe deb http://archive.ubuntu.com/ubuntu/ bionic-updates universe deb http://archive.ubuntu.com/ubuntu/ bionic multiverse deb http://archive.ubuntu.com/ubuntu/ bionic-updates multiverse deb http://archive.ubuntu.com/ubuntu/ bionic-backports main restricted universe multiverse deb http://security.ubuntu.com/ubuntu/ bionic-security main restricted deb http://security.ubuntu.com/ubuntu/ bionic-security universe deb http://security.ubuntu.com/ubuntu/ bionic-security multiverse
*/
