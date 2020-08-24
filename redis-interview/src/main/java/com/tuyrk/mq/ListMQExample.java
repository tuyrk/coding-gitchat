package com.tuyrk.mq;

import redis.clients.jedis.Jedis;

/**
 * 消息队列的具体实现：List 版
 */
public class ListMQExample {
  private static final String CHANNEL = "mq";

  public static void main(String[] args) throws InterruptedException {
    new Thread(ListMQExample::bConsumer).start(); // 消费者
    producer(); // 生产者
  }

  /**
   * 生产者
   */
  public static void producer() throws InterruptedException {
    Jedis jedis = new Jedis("127.0.0.1", 6379);
    // 推送消息
    jedis.lpush(CHANNEL, "Hello, List.");
    Thread.sleep(1000);
    jedis.lpush(CHANNEL, "message 2.");
    Thread.sleep(2000);
    jedis.lpush(CHANNEL, "message 3.");
  }

  /**
   * 消费者（阻塞版）
   */
  public static void bConsumer() {
    Jedis jedis = new Jedis("127.0.0.1", 6379);
    while (true) {
      // 阻塞读
      for (String item : jedis.brpop(0, CHANNEL)) {
        // 读取到相关数据，进行业务处理
        System.out.println(item);
      }
    }
  }
}
