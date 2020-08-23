# Redis 如何实现分布式锁？

锁是多线程编程中的一个重要概念，它是保证多线程并发时顺利执行的关键。通常所说的“锁”是指程序中的锁，也就是单机锁，例如 Java 中的 Lock 和 ReadWriteLock 等，而分布式锁是指可以使用在多机集群环境中的锁。

本文的面试题是：使用 Redis 如何实现分布式锁？

### 典型回答

Redis 作为一个独立的三方系统（通常被作为缓存中间件使用），其天生的优势就是可以作为一个分布式系统来使用，因此使用 Redis 实现的锁都是分布式锁，理解了这个概念才能看懂本文所说的内容。

分布式锁的示意图，如下所示： 

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi128lc3hsj30og0eo3yz.jpg" alt="分布式锁的示意图" style="zoom:70%;" />

使用 Redis 实现分布式锁可以通过以下两种手段来实现：

- 使用 incr + del 方式实现
- 使用 setnx + del 方式实现

#### 使用 incr + del 方式实现

有人可能会奇怪 incr 不是用来实现数值 +1 操作的吗？用它怎么来实现分布式锁呢？

首先来看 incr 的使用示例：

```shell
> set key 1 # 新增一个键值
OK
> incr key # 执行加 1 操作
(integer) 2
> get key # 查询键值
"2"
```

从以上代码可以看出使用 incr 可以实现数值 +1，那怎么用它来实现分布式锁呢？

其实原理也很简单，**每次的加锁（上锁）都使用 incr 命令，如果执行的结果为 1 的话表示加锁成功，释放锁则使用 del 命令来实现**。实现示例如下：

```shell
> incr lock # 加锁
(integer) 1
> del lock # 释放锁
(integer) 0
```

当某个程序正在使用锁时，继续使用 incr 会导致返回的结果不为 1，如下命令所示：

```shell
> incr key # 第一次加锁
(integer) 1
> incr key # 第二次加锁
(integer) 2
```

从以上命令可以看出，当一个程序正在使用锁时，再进行加锁操作就会导致结果不为 1，就可以判断此锁正在被使用中，这样就可以实现分布式的功能了。

#### 使用 setnx + del 方式实现

使用 setnx(set if not exists) 命令道理也是相同的，当**使用 setnx 创建键值成功时，则表明加锁成功，否则既代码加锁失败**。实现示例如下：

```shell
> setnx lock true
(integer) 1 #创建锁成功
#逻辑业务处理...
> del lock
(integer) 1 #释放锁
```

当我们重复加锁时执行结果如下：

```shell
> setnx lock true # 第一次加锁
(integer) 1
> setnx lock true # 第二次加锁
(integer) 0
```

从上述命令可以看出，可以**通过执行的结果是否为 1 来判断加锁是否成功**。

### 考点分析

分布式锁的概念虽然看起来很“高大上”，其实并没有想的那么难。上面通过 incr 和 setnx 的方式来实现了分布式锁，然而真正的分布式锁考察的知识点远远不止这些，比如分布式锁的死锁问题？如何用 Java 代码来实现分布式锁等。

### 知识扩展

#### 分布式锁的死锁问题

由于 incr 和 setnx 的方式比较类似，因此使用 setnx 来说一下死锁的问题。从上面的命令可以看出，只是使用了最原始的方式实现了分布式锁的功能，然而在具体实现的过程中还需要考虑“死锁”的问题。

死锁是并发编程中比较常见的问题，以单机锁的死锁来说，当两个线程都持有了自己锁资源并试图获取对方锁资源时就会造成死锁的诞生，如下图所示： 

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi128jgk05j30it0e53yr.jpg" alt="单机锁的死锁" style="zoom:60%;" />

为了更好的帮助大家理解死锁的概念，这里提供了一个 Java 版本单机锁死锁的实现示例。如下所示：

```java
Object obj1 = new Object();
Object obj2 = new Object();
// 线程 1 拥有对象 1，想要等待获取对象 2
new Thread(() -> {
  synchronized (obj1) {
    Thread.sleep(1000);
    synchronized (obj2) {
      System.out.println(Thread.currentThread().getName());
    }
  }
}).start();
// 线程 2 拥有对象 2，想要等待获取对象 1
new Thread(() -> {
  synchronized (obj2) {
    Thread.sleep(1000);
    synchronized (obj1) {
      System.out.println(Thread.currentThread().getName());
    }
  }
}).start();
```

回到本文主题，分布式锁的死锁是如何发生的呢？

在系统中，当程序创建了分布式锁后，因为某些特殊原因导致程序意外退出，那么这个**锁将永远不会被释放**，就造成了死锁的问题。

解决死锁最简单的方式：设置锁的过期时间。这样即使出现了程序意外退出的情况，在等待此锁超过了设置的过期时间后就会释放此锁，其他程序就可以继续使用了。

那么，最简单的方式就是使用 expire key seconds 命令来设置。示例代码如下：

```shell
> setnx lock true
(integer) 1
> expire lock 30
(integer) 1
# 逻辑业务处理...
> del lock
(integer) 1 # 释放锁
```

但这样依然会有问题，因为命令 setnx 和 expire 处理是一前一后非原子性的，因此如果在它们执行之间，出现断电和 Redis 异常退出的情况，因为超时时间未设置，依然会造成死锁。

然而在 Redis 2.6.12 版本后新增了可以使用原子操作，也就是一条命令来执行 setnx 和 expire 操作了。实现示例如下：

```powershell
> set lock true ex 30 nx
OK # 创建锁成功
> set lock true ex 30 nx
(nil) # 在锁被占用的时候，企图获取锁失败
```

其中 ex 为设置超时时间， nx 为元素非空判断，用来判断是否能正常使用锁。

#### 使用代码实现分布式锁

上面通过 `set` 命令同时执行了 setnx 和 expire 的操作，好像一切问题都解决了，然而并没有那么简单。使用 `set` 命令只解决创建锁的问题，在执行中的极端问题和释放锁极端问题，依旧要考虑。

例如，设置锁的最大超时时间是 30s，但业务处理需要使用 35s。这就会导致原有的业务还未执行完成，锁就被释放了，新的程序和旧程序一起操作就会带来线程安全的问题。

此执行流程如下图所示：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi128kkctij30ai07l0sm.jpg" alt="分布式锁-执行超时同时拥有锁" style="zoom:100%;" />

**执行超时**问题除带来**线程安全**问题外，还引发了另一个问题：**锁被误删**。 假设锁的最大超时时间是 30s，应用 1 需要执行 35s，然而在 30s时，锁被自动释放后，应用 2 重新获取并设置了锁，然后在 35s 时，应用 1 执行完成后，就会把应用 2 创建的锁给删除掉。如下图所示： 

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi128jzzn8j30b40gljrd.jpg" alt="分布式锁-锁被误删" style="zoom:80%;" />

**锁被误删的解决方案**是在使用 `set` 命令创建锁时，给 value 值设置一个归属人标识，例如给应用关联一个 UUID，每次在删除之前先要判断 UUID 是不是属于当前的线程，如果属于则删除，这就避免了锁被误删的问题。 注意：如果是在代码中执行删除，不能使用先判断再删除的方法，伪代码如下：

```java
if(xxx.equals(xxx)){ // 判断是否是自己的锁
  del(luck); // 删除锁
}
```

判断代码和删除代码不具备原子性，因此也不能这样使用，这个时候可以使用 Lua 脚本来执行判断和删除的操作，因为多条 Lua 命令可以保证原子性，Java 实现代码如下：

```java
/**
 * 释放分布式锁
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
```

其中，Collections.singletonList() 方法的作用是将 String 转成 List，因为 jedis.eval() 最后两个参数的类型必须是 List。

说完了锁误删的解决方案，回过头来看如何解决执行超时的问题，执行超时的问题可以从以下两方面来解决：

1. 把执行比较耗时的任务不要放到加锁的方法内，锁内的方法尽量控制执行时长
2. 适当的把最大超时时间设置的长一点，正常情况下锁用完之后会被手动的删除掉

接下来，我们使用 Java 语言来实现一个完整的分布式锁，代码如下：

```java
public class DistributedLockExample {
  private static final String LOCK_KEY = "REDIS_LOCK"; // 锁 key
  private static final String FLAG_ID = "UUID:6379";  // 标识（UUID）
  private static final Integer TIME_OUT = 90;     // 最大超时时间

  public static void main(String[] args) {
    Jedis jedis = JedisUtils.getJedis();
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
```

以上代码执行结果如下所示：

```
加锁成功
锁释放成功
```

### 总结

本文介绍了单机锁和分布式锁的概念，以及分布式的两种实现方式 incr 和 setnx 方式；同时介绍了分布式锁存在的两个问题（死锁和锁误删）以及它们的解决方案，最后使用 Java 代码和 Lua 脚本来实现了完整的分布式锁程序。