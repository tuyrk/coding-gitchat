# 使用 Redis 如何实现延迟队列？

延迟消息队列在日常工作中经常会被用到，比如支付系统中超过 30 分钟未支付的订单，将会被取消，这样就可以保证此商品库存可以释放给其他人购买；还有外卖系统如果商家超过 5 分钟未接单的订单，将会被自动取消，以此来保证用户可以更及时的吃到自己点的外卖。等等诸如此类的业务场景都需要使用到延迟消息队列，又因为它在业务中比较常见，因此这个知识点在面试中也会经常被问到。

本文的面试题是：使用 Redis 如何实现延迟消息队列？

### 典型回答

延迟消息队列的常见实现方式是通过 ZSet 的存储与查询来实现，它的核心思想是在程序中开启一个一直循环的延迟任务的检测器，用于检测和调用延迟任务的执行。如下图所示： 

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi1xy85uvmj30w40ccjru.jpg" alt="延迟消息队列.jpg" width="580" />

ZSet 实现延迟任务的方式有两种：

- 利用 `zrangebyscore` 查询符合条件的所有待处理任务，循环执行队列任务
- 每次查询最早的一条消息，判断这条信息的执行时间是否小于等于此刻的时间，如果是则执行此任务，否则继续循环检测。

#### 方式一：zrangebyscore 查询所有任务

此实现方式是一次性查询出所有的延迟任务，然后再进行执行。实现代码如下：

延迟队列消费：

```java
public static void doDelayQueue(Jedis jedis) throws InterruptedException {
  while (true) {
    Instant nowInstant = Instant.now(); // 当前时间
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
```

添加消息：

```java
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
```

以上程序执行结果如下：

```
消费：order_2
消费：order_3
消费：order_4
消费：order_5
消费：order_1
```

#### 方式二：判断最早的任务

此实现方式是每次查询最早的一条任务，再与当前时间进行判断，如果任务执行时间大于当前时间则表示应该立即执行延迟任务。实现代码如下：

```java
public static void doDelayQueue2(Jedis jedis) throws InterruptedException {
  while (true) {
    long nowSecond = Instant.now().getEpochSecond(); // 当前时间
    // 每次查询一条消息，判断此消息的执行时间
    Set<String> data = jedis.zrange(ZSET_KEY, 0, 0);
    if (data.size() == 1) {
      String firstValue = data.iterator().next();
      // 消息执行时间
      Double score = jedis.zscore(ZSET_KEY, firstValue);
      if (score <= nowSecond) {
        // 消费消息（业务功能处理）
        System.out.println("消费消息：" + firstValue);
        // 删除已经执行的任务
        jedis.zrem(ZSET_KEY, firstValue);
      }
    }
    Thread.sleep(100); // 执行间隔
  }
}
```

以上程序执行结果和实现方式一相同，结果如下：

```
消费：order_2
消费：order_3
消费：order_4
消费：order_5
消费：order_1
```

其中，执行间隔代码 `Thread.sleep(100)` 可根据实际的业务情况删减或配置。

### 考点分析

延迟消息队列的实现方法有很多种，不同的公司可能使用的技术也是不同的。上面是从 Redis 的角度出发来实现了延迟消息队列，但一般面试官不会就此罢休，会借着这个问题来问关于更多的延迟消息队列的实现方法，因此除了 Redis 实现延迟消息队列的方式，还需要具备一些其他的常见的延迟队列的实现方法。

和此知识点相关的面试题还有以下这些：

- 使用 Java 语言如何实现一个延迟消息队列？
- 你还知道哪些实现延迟消息队列的方法？

### 知识扩展

#### Java 中的延迟消息队列

可以使用 Java 语言中自带的 DelayQueue 数据类型来实现一个延迟消息队列。实现代码如下：

```java
static class DelayElement implements Delayed {
  // 延迟截止时间（单面：毫秒）
  long delayTime = System.currentTimeMillis();
  public DelayElement(long delayTime) {
    this.delayTime = (this.delayTime + delayTime);
  }

  // 获取剩余时间
  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(delayTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  // 队列里元素的排序依据
  @Override
  public int compareTo(Delayed o) {
    long taskDelay = this.getDelay(TimeUnit.MILLISECONDS);
    long curDelay = o.getDelay(TimeUnit.MILLISECONDS);
    return Long.compare(taskDelay, curDelay);
  }

  @Override
  public String toString() {
    return DateFormat.getDateTimeInstance().format(new Date(delayTime));
  }
}
```

```java
DelayQueue<DelayElement> delayQueue = new DelayQueue<>();
delayQueue.put(new DelayElement(1000));
delayQueue.put(new DelayElement(3000));
delayQueue.put(new DelayElement(5000));

System.out.println("开始时间：" + DateFormat.getDateTimeInstance().format(new Date()));
while (!delayQueue.isEmpty()) {
  System.out.println(delayQueue.take());
}
System.out.println("结束时间：" + DateFormat.getDateTimeInstance().format(new Date()));
```

以上程序执行的结果如下：

```
开始时间：2020-8-26 14:16:16
2020-8-26 14:16:17
2020-8-26 14:16:19
2020-8-26 14:16:21
结束时间：2020-8-26 14:16:21
```

此实现方式的优点是开发比较方便，可以直接在代码中使用，实现代码也比较简单，但它缺点是数据保存在内存中，因此可能存在数据丢失的风险，最大的问题是它无法支持分布式系统。

#### 使用 MQ 实现延迟消息队列

使用主流的 MQ 中间件也可以方便的实现延迟消息队列的功能。比如 RabbitMQ，可以通过它的 rabbitmq-delayed-message-exchange 插件来实现延迟队列。实现代码如下：

配置并开启 rabbitmq-delayed-message-exchange 插件：

```shell
# 下载rabbitmq-delayed-message-exchange插件
wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.8.0/rabbitmq_delayed_message_exchange-3.8.0.ez
# 将插件文件上传至docker容器
docker cp rabbitmq_delayed_message_exchange-3.8.0.ez rabbitmq:/plugins
# 进入docker容器命令行
docker exec -it rabbitmq bash
# 启动插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
# 重启rabbitmq服务
docker restart rabbitmq
```

添加rabbitmq依赖：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

配置消息队列：

```java
@Configuration
public class DelayedConfig {
  public final static String QUEUE_NAME = "delayed.goods.order";
  public final static String EXCHANGE_NAME = "delayedec";

  @Bean
  public Queue queue() {
    return new Queue(QUEUE_NAME);
  }

  // 配置默认的交换机
  @Bean
  public CustomExchange customExchange() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-delayed-type", "direct");
    // 参数二为类型：必须是 x-delayed-message
    return new CustomExchange(DelayedConfig.EXCHANGE_NAME, "x-delayed-message", true, false, args);
  }

  // 绑定队列到交换器
  @Bean
  public Binding binding(Queue queue, CustomExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(DelayedConfig.QUEUE_NAME).noargs();
  }
}
```

消息发送者：

```java
@Component
public class DelayedSender {
  @Autowired
  private AmqpTemplate rabbitTemplate;

  public void send(String msg) {
    rabbitTemplate.convertAndSend(DelayedConfig.EXCHANGE_NAME, DelayedConfig.QUEUE_NAME, msg, message -> {
      // 配置 3s 后再进行任务执行
      message.getMessageProperties().setHeader("x-delay", 3000);
      System.out.println("发送时间：" + System.currentTimeMillis());
      return message;
    });
  }
}
```

消息消费者：

```java
@Component
@RabbitListener(queues = DelayedConfig.QUEUE_NAME)
public class DelayedReceiver {
  @RabbitHandler
  public void process(String msg) {
    System.out.println("接收时间：" + System.currentTimeMillis());
    System.out.println("消息内容：" + msg);
  }
}
```

测试代码：

```java
@Autowired
private DelayedSender sender;

@Test
public void test() throws InterruptedException {
  sender.send("Hi Admin.");
  TimeUnit.SECONDS.sleep(5); // 等待接收程序执行之后，再退出测试
}
```

以上程序执行的结果如下：

```
发送时间：1598425953174
接收时间：1598425956187
消息内容：Hi Admin.
```

从上述结果可以看出：当消息进入延迟队列 3s 后才被正常消费，执行结果符合预期，RabbitMQ 成功的实现了延迟消息队列。

### 总结

本文讲了延迟消息队列的两种使用场景：支付系统中的超过 30 分钟未支付的订单，将会被自动取消，以此来保证此商品的库存可以正常释放给其他人购买，还有外卖系统如果商家超过 5 分钟未接单的订单，将会被自动取消，以此来保证用户可以更及时的吃到自己点的外卖。并且还讲了延迟队列的 4 种实现方式，使用 ZSet 的 2 种实现方式，以及 Java 语言中的 DelayQueue 的实现方式，还有 RabbitMQ 的插件 rabbitmq-delayed-message-exchange 的实现方式。
