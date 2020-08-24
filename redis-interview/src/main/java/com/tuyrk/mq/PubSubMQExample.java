package com.tuyrk.mq;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * 消息队列的具体实现：Pub/Sub 实现方式
 */
public class PubSubMQExample {
    private static final String CHANNEL = "channel";

    public static void main(String[] args) throws InterruptedException {
        new Thread(PubSubMQExample::consumer).start(); // 创建一个新线程作为消费者
        Thread.sleep(500); // 暂停 0.5s 等待消费者初始化
        producer(); // 生产者发送消息
    }

    /**
     * 生产者
     */
    public static void producer() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        // 推送消息
        jedis.publish(CHANNEL, "Hello, channel.");
    }

    /**
     * 消费者
     */
    public static void consumer() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        // 接收并处理消息
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                // 接收消息，业务处理
                System.out.println("频道 " + channel + " 收到消息：" + message);
            }
        }, CHANNEL);
    }
}
