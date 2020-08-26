package com.tuyrk.delay;

import com.tuyrk.util.JedisUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.Set;

/**
 * 延迟队列
 * redis实现延迟队列
 */
public class DelayQueueByRedisTest {
    // zset key
    private static final String ZSET_KEY = "myDelayQueue";

    @Test
    public void testDelayByTime() throws InterruptedException {
        doDelayQueue(addMessage());
    }

    @Test
    public void testDelayByOrder() throws InterruptedException {
        doDelayQueue2(addMessage());
    }

    /**
     * 添加消息
     */
    private Jedis addMessage() {
        Jedis jedis = JedisUtil.getJedis();
        // 延迟 30s 执行（30s 后的时间）
        long delayTime = Instant.now().plusSeconds(30).getEpochSecond();
        jedis.zadd(ZSET_KEY, delayTime, "order_1");
        // 继续添加测试数据
        jedis.zadd(ZSET_KEY, Instant.now().plusSeconds(2).getEpochSecond(), "order_2");
        jedis.zadd(ZSET_KEY, Instant.now().plusSeconds(2).getEpochSecond(), "order_3");
        jedis.zadd(ZSET_KEY, Instant.now().plusSeconds(7).getEpochSecond(), "order_4");
        jedis.zadd(ZSET_KEY, Instant.now().plusSeconds(10).getEpochSecond(), "order_5");
        return jedis;
    }


    /**
     * 方式一：zrangebyscore 查询所有任务
     *
     * @param jedis Redis 客户端
     */
    public static void doDelayQueue(Jedis jedis) throws InterruptedException {
        while (true) {
            // 当前时间
            Instant nowInstant = Instant.now();
            long lastSecond = nowInstant.minusSeconds(1).getEpochSecond(); // 上一秒时间
            long nowSecond = nowInstant.getEpochSecond();
            // 查询当前时间的所有任务
            Set<String> data = jedis.zrangeByScore(ZSET_KEY, lastSecond, nowSecond);
            for (String item : data) {
                // 消费任务
                System.out.println("消费：" + item);
            }
            // 删除已经执行的任务
            jedis.zremrangeByScore(ZSET_KEY, lastSecond, nowSecond);
            Thread.sleep(1000); // 每秒轮询一次
        }
    }

    /**
     * 方式二：判断最早的任务
     *
     * @param jedis Redis 客户端
     */
    public static void doDelayQueue2(Jedis jedis) throws InterruptedException {
        while (true) {
            // 当前时间
            long nowSecond = Instant.now().getEpochSecond();
            // 每次查询一条消息，判断此消息的执行时间
            Set<String> data = jedis.zrange(ZSET_KEY, 0, 0);
            if (data.size() == 1) {
                String firstValue = data.iterator().next();
                // 消息执行时间
                Double score = jedis.zscore(ZSET_KEY, firstValue);
                if (nowSecond >= score) {
                    // 消费消息（业务功能处理）
                    System.out.println("消费消息：" + firstValue);
                    // 删除已经执行的任务
                    jedis.zrem(ZSET_KEY, firstValue);
                }
            }
            Thread.sleep(100); // 执行间隔
        }
    }
}
