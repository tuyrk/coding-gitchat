package com.tuyrk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntry;
import redis.clients.jedis.StreamEntryID;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息队列的具体实现：Stream 实现方式
 */
public class StreamGroupExample {
  private static final String STREAM_KEY = "mq"; // 流 key
  private static final String GROUP_NAME = "g1"; // 分组名称
  private static final String CONSUMER_1_NAME = "c1"; // 消费者 1 的名称
  private static final String CONSUMER_2_NAME = "c2"; // 消费者 2 的名称

  public static void main(String[] args) {
    producer(); // 生产者
    createGroup(STREAM_KEY, GROUP_NAME); // 创建消费组
    new Thread(StreamGroupExample::consumer1).start(); // 消费者 1
    new Thread(StreamGroupExample::consumer2).start(); // 消费者 2
  }

  /**
   * 创建消费分组
   *
   * @param stream    流 key
   * @param groupName 分组名称
   */
  public static void createGroup(String stream, String groupName) {
    Jedis jedis = JedisUtils.getJedis();
    jedis.xgroupCreate(stream, groupName, new StreamEntryID(), true);
  }

  /**
   * 生产者
   */
  public static void producer() {
    Jedis jedis = JedisUtils.getJedis();
    // 添加消息 1
    Map<String, String> map1 = new HashMap<>();
    map1.put("data", "redis");
    StreamEntryID id = jedis.xadd(STREAM_KEY, null, map1);
    System.out.println("消息添加成功 ID：" + id);
    // 添加消息 2
    Map<String, String> map2 = new HashMap<>();
    map2.put("data", "java");
    StreamEntryID id2 = jedis.xadd(STREAM_KEY, null, map2);
    System.out.println("消息添加成功 ID：" + id2);
  }

  /**
   * 消费者 1
   */
  public static void consumer1() {
    Jedis jedis = JedisUtils.getJedis();
    // 消费消息
    while (true) {
      // 读取消息
      Map.Entry<String, StreamEntryID> entry = new AbstractMap.SimpleImmutableEntry<>(STREAM_KEY, StreamEntryID.UNRECEIVED_ENTRY);
      // 阻塞读取一条消息（最大阻塞时间 120s）
      List<Map.Entry<String, List<StreamEntry>>> list = jedis.xreadGroup(GROUP_NAME, CONSUMER_1_NAME, 1, 120 * 1000, true, entry);
      if (list != null && list.size() == 1) {
        // 读取到消息
        Map<String, String> content = list.get(0).getValue().get(0).getFields(); // 消息内容
        System.out.println("Consumer 1 读取到消息 ID：" + list.get(0).getValue().get(0).getID() + " 内容：" + content);
      }
    }
  }

  /**
   * 消费者 2
   */
  public static void consumer2() {
    Jedis jedis = JedisUtils.getJedis();
    // 消费消息
    while (true) {
      // 读取消息
      Map.Entry<String, StreamEntryID> entry = new AbstractMap.SimpleImmutableEntry<>(STREAM_KEY, StreamEntryID.UNRECEIVED_ENTRY);
      // 阻塞读取一条消息（最大阻塞时间 120s）
      List<Map.Entry<String, List<StreamEntry>>> list = jedis.xreadGroup(GROUP_NAME, CONSUMER_2_NAME, 1, 120 * 1000, true, entry);
      if (list != null && list.size() == 1) {
        // 读取到消息
        Map<String, String> content = list.get(0).getValue().get(0).getFields(); // 消息内容
        System.out.println("Consumer 2 读取到消息 ID：" + list.get(0).getValue().get(0).getID() + " 内容：" + content);
      }
    }
  }
}
