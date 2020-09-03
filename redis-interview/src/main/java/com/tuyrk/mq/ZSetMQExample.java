package com.tuyrk.mq;

import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * 消息队列的具体实现：ZSet 版
 */
public class ZSetMQExample {
    private static final String CHANNEL = "mq";

    public static void main(String[] args) throws InterruptedException {
        new Thread(ZSetMQExample::bConsumer).start(); // 消费者
        producer(); // 生产者
    }

    /**
     * 生产者
     */
    public static void producer() throws InterruptedException {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        // 推送消息
        jedis.zadd(CHANNEL, System.currentTimeMillis(), "Hello, List.");
        TimeUnit.SECONDS.sleep(1);
        jedis.zadd(CHANNEL, System.currentTimeMillis(), "message 2.");
        TimeUnit.SECONDS.sleep(2);
        jedis.zadd(CHANNEL, System.currentTimeMillis(), "message 3.");
    }

    /**
     * 消费者（阻塞版）
     */
    public static void bConsumer() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        while (true) {
            // 阻塞读
            for (String item : jedis.zrangeByScore(CHANNEL, 0, System.currentTimeMillis(), 0, 1)) {
                System.out.println("item = " + item);
                jedis.zrem(CHANNEL, item);
            }

        }
    }
}
