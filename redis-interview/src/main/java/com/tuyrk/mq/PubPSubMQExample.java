package com.tuyrk.mq;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * 消息队列的具体实现：Pub/Sub 主题订阅模式实现方式
 */
public class PubPSubMQExample {
  private static final String CHANNEL = "channel";

  public static void main(String[] args) throws InterruptedException {
    new Thread(PubPSubMQExample::pConsumer).start(); // 主题订阅
    Thread.sleep(500); // 暂停 0.5s 等待消费者初始化
    producer1(); // 生产者1发送消息
    producer2(); // 生产者2发送消息
  }

  /**
   * 生产者1
   */
  public static void producer1() {
    Jedis jedis = new Jedis("127.0.0.1", 6379);
    // 推送消息
    jedis.publish(CHANNEL.concat("_p1"), "Hello, channel1.");
  }

  /**
   * 生产者2
   */
  public static void producer2() {
    Jedis jedis = new Jedis("127.0.0.1", 6379);
    // 推送消息
    jedis.publish(CHANNEL.concat("_p2"), "Hello, channel2.");
  }

  /**
   * 消费者，主题订阅
   */
  public static void pConsumer() {
    Jedis jedis = new Jedis("127.0.0.1", 6379);
    // 主题订阅
    jedis.psubscribe(new JedisPubSub() {
      @Override
      public void onPMessage(String pattern, String channel, String message) {
        // 接收消息，业务处理
        System.out.println(pattern + " 主题 | 频道 " + channel + " 收到消息：" + message);
      }
    }, CHANNEL.concat("*"));
  }
}
