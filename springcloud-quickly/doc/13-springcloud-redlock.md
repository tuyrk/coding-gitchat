# 第12课：分布式锁

本课讲述的是基于 Spring Cloud 的分布式架构，那么也带来了线程安全问题，比如一个商城系统，下单过程可能由不同的微服务协作完成，在高并发的情况下如果不加锁就会有问题，而传统的加锁方式只针对单一架构，对于分布式架构是不适合的，这时就需要用到分布式锁。

实现分布式锁的方式有很多，本文结合我的实际项目和目前的技术趋势，通过实例实现几种较为流行的分布式锁方案，最后会对不同的方案进行比较。(三种：基于 Redis 的分布式锁、基于数据库的分布式锁、基于 Zookeeper 的分布式锁)

### 基于 Redis 的分布式锁

#### 利用 SETNX 和 SETEX(了解)

基本命令主要有：

- SETNX(SET If Not Exists)：当且仅当 Key 不存在时，则可以设置，否则不做任何动作。
- SETEX：可以设置超时时间

其原理为：通过 SETNX 设置 Key-Value 来获得锁，随即进入死循环，每次循环判断，如果存在 Key 则继续循环，如果不存在 Key，则跳出循环，当前任务执行完成后，删除 Key 以释放锁。

这种方式可能会导致死锁，为了避免这种情况，需要设置超时时间。

下面，请看具体的实现步骤。

1. 创建一个 Maven 工程并在 pom.xml 加入以下依赖：

   ```xml
   <dependency><!-- redis-->
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-redis</artifactId>
   </dependency>
   ```

2. 创建启动类 Application.java

3. 添加配置文件 application.yml：

   ```yaml
   spring:
     redis:
       host: localhost
       port: 6379
   ```

4. 创建全局锁类 Lock.java：

   ```java
   @Getter
   @AllArgsConstructor
   public class Lock {
     private String name;
     private String value;
   }
   ```

5. 创建分布式锁类 DistributedLockHandler.java：

   > @param lock                     锁的名称
   > @param timeout              获取的超时时间
   > @param tryInterval         多少ms尝试一次
   > @param lockExpireTime 获取成功后锁的过期时间
   > @return true 获取成功，false获取失败

   ```java
   @Component
   @Slf4j
   public class DistributedLockHandler {
     @Autowired
     private StringRedisTemplate template;
   
     // 操作redis获取全局锁
     public boolean getLock(Lock lock, long timeout, long tryInterval, long lockExpireTime) {
       if (StringUtils.isEmpty(lock.getName()) || StringUtils.isEmpty(lock.getValue())) {
         return false;
       }
       long startTime = System.currentTimeMillis();
       try {
         do {
           if (!template.hasKey(lock.getName())) { // 不存在锁，则创建锁并返回
             ValueOperations<String, String> ops = template.opsForValue();
             ops.set(lock.getName(), lock.getValue(), lockExpireTime, TimeUnit.MILLISECONDS);
             return true;
           }
           log.debug("lock is exist!！！");
           // 尝试超过了设定值之后直接跳出循环
           if (System.currentTimeMillis() - startTime > timeout) {
             return false;
           }
           Thread.sleep(tryInterval);
         }
         while (template.hasKey(lock.getName()));
       } catch (InterruptedException e) {
         log.error(e.getMessage());
         return false;
       }
       return false;
     }
     // 释放锁
     public void releaseLock(Lock lock) {
       if (!StringUtils.isEmpty(lock.getName())) {
         template.delete(lock.getName());
       }
     }
   }
   ```

6. 最后创建 HelloController 来测试分布式锁。

   ```java
   @RestController
   public class HelloController {
     @Autowired
     private DistributedLockHandler distributedLockHandler;
   
     @RequestMapping("index")
     public String index() throws Exception {
       Lock lock = new Lock("lynn", "min");
       if (distributedLockHandler.tryLock(lock)) {
         // 为了演示锁的效果，这里睡眠5000毫秒
         System.out.println("执行方法");
         Thread.sleep(5000);
         distributedLockHandler.releaseLock(lock);
       }
       return "hello world!";
     }
   }
   ```

7. 测试。

   启动 Application.java，连续访问两次浏览器：http://localhost:8080/index，控制台可以发现先打印了一次“执行方法”，说明后面一个线程被锁住了，5秒后又再次打印了“执行方法”，说明锁被成功释放。

> 通过这种方式创建的分布式锁存在以下问题：
>
> 1. 高并发的情况下，如果两个线程同时进入循环，可能导致加锁失败。
> 2. SETNX 是一个耗时操作，因为它需要判断 Key 是否存在，因为会存在性能问题。

因此，**Redis 官方推荐 Redlock 来实现分布式锁**。

#### 利用 Redlock(重点)

通过 Redlock 实现分布式锁比其他算法更加可靠，继续改造上一例的代码。

1. pom.xml 增加以下依赖：

   ```xml
   <dependency>
     <groupId>org.redisson</groupId>
     <artifactId>redisson</artifactId>
     <version>3.13.2</version>
   </dependency>
   ```

2. 增加以下几个类：

   - 获取锁后需要处理的逻辑

   ```java
   public interface AquiredLockWorker<T> {
       T invokeAfterLockAquire() throws Exception;
   }
   ```

   - 获取RedissonClient连接类

   ```java
   @Component
   public class RedissonConnector {
     private RedissonClient redisson;
     @PostConstruct
     public void init() {
       redisson = Redisson.create();
     }
     
     public RedissonClient getClient() { return redisson; }
   }
   ```

   ```java
   @Component
   public class RedisLocker {
     private final static String LOCKER_PREFIX = "lock:";
     @Autowired
     private RedissonConnector redissonConnector;
     
     /**
      * 获取锁
      * @param resourceName  锁的名称
      * @param worker 获取锁后的处理类
      * @return 处理完具体的业务逻辑要返回的数据
      */
     @Override
     public <T> T lock(String resourceName, AquiredLockWorker<T> worker) throws Exception {
       return lock(resourceName, worker, 100);
     }
     @Override
     public <T> T lock(String resourceName, AquiredLockWorker<T> worker, int lockTime) throws Exception {
       RedissonClient redisson= redissonConnector.getClient();
       RLock lock = redisson.getLock(LOCKER_PREFIX + resourceName);
       // Wait for 100 seconds seconds and automatically unlock it after lockTime seconds
       boolean success = lock.tryLock(100, lockTime, TimeUnit.SECONDS);
       if (success) {
         try {
           return worker.invokeAfterLockAquire();
         } finally {
           lock.unlock();
         }
       }
       throw new RuntimeException();
     }
   }
   ```

3. 修改 HelloController：

   ```java
   @RestController
   public class HelloController {
     @Autowired
     private RedisLocker redisLocker;
   
     @RequestMapping("index")
     public String index() throws Exception {
       redisLocker.lock("test", () -> {
         System.out.println("执行方法！");
         Thread.sleep(5000);
         return null;
       });
       return "hello world!";
     }
   }
   ```

4. 按照上节的测试方法进行测试，我们发现分布式锁也生效了。

Redlock 是 Redis 官方推荐的一种方案，因此可靠性比较高。

### 基于数据库的分布式锁

#### 基于数据库表(了解)

它的基本原理和 Redis 的 SETNX 类似，其实就是**创建一个分布式锁表**，**加锁就在表增加一条记录，释放锁即把该数据删掉**，具体实现，我这里就不再一一举出。

它同样存在一些**问题**：

1. 没有失效时间，容易导致死锁；
2. 依赖数据库的可用性，一旦数据库挂掉，锁就马上不可用；
3. 这把锁只能是非阻塞的，因为数据的 insert 操作，一旦插入失败就会直接报错。没有获得锁的线程并不会进入排队队列，要想再次获得锁就要再次触发获得锁操作；
4. 这把锁是非重入的，同一个线程在没有释放锁之前无法再次获得该锁。因为数据库中数据已经存在了。

#### 乐观锁(了解)

基本原理为：乐观锁一般通过 version 来实现，也就是在数据库表创建一个 version 字段，每次更新成功，则 version+1，读取数据时，我们将 version 字段一并读出，每次更新时将会对版本号进行比较，如果一致则执行此操作，否则更新失败！

#### 悲观锁（排他锁）

1. 创建一张数据库表：

   ```mysql
   CREATE TABLE `methodLock` (
     `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
     `method_name` varchar(64) NOT NULL DEFAULT '' COMMENT '锁定的方法名',
     `desc` varchar(1024) NOT NULL DEFAULT '备注信息',
     `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '保存数据时间，自动生成',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uidx_method_name` (`method_name `) USING BTREE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='锁定中的方法';
   ```

2. 通过数据库的排他锁来实现分布式锁。

   基于 MySQL 的 InnoDB 引擎，可以使用以下方法来实现加锁操作：

   ```java
   public boolean lock() {
     connection.setAutoCommit(false)
       while(true) {
         try {
           result = select * from methodLock where method_name=xxx for update;
           if(result == null) {
             return true;
           }
         } catch(Exception e) { }
         sleep(1000);
       }
     return false;
   }
   ```

3. 我们可以认为获得排它锁的线程即可获得分布式锁，当获取到锁之后，可以执行方法的业务逻辑，执行完方法之后，再通过以下方法解锁：

   ```java
   public void unlock(){
       connection.commit();
   }
   ```

### 基于 Zookeeper 的分布式锁

#### ZooKeeper 简介

ZooKeeper 是一个分布式的，开放源码的分布式应用程序协调服务，是 Google Chubby 的一个开源实现，是 Hadoop 和 Hbase 的重要组件。它是一个为分布式应用提供一致性服务的软件，提供的功能包括：配置维护、域名服务、分布式同步、组服务等。

#### 分布式锁实现原理

实现原理为：

1. 建立一个节点，假如名为 lock 。节点类型为持久节点（Persistent）
2. 每当进程需要访问共享资源时，会调用分布式锁的 lock() 或 tryLock() 方法获得锁，这个时候会在第一步创建的 lock 节点下建立相应的顺序子节点，节点类型为临时顺序节点（`EPHEMERAL_SEQUENTIAL`），通过组成特定的名字 name+lock+顺序号。
3. 在建立子节点后，对 lock 下面的所有以 name 开头的子节点进行排序，判断刚刚建立的子节点顺序号是否是最小的节点，假如是最小节点，则获得该锁对资源进行访问。
4. 假如不是该节点，就获得该节点的上一顺序节点，并监测该节点是否存在注册监听事件。同时在这里阻塞。等待监听事件的发生，获得锁控制权。
5. 当调用完共享资源后，调用 unlock() 方法，关闭 ZooKeeper，进而可以引发监听事件，释放该锁。

实现的分布式锁是严格的按照顺序访问的并发锁。

#### 代码实现

1. 创建 DistributedLock 类：

```java
public class DistributedLock implements Lock, Watcher {
  private ZooKeeper zk;
  private String root = "/locks"; // 根
  private String lockName; // 竞争资源的标志
  private String waitNode; // 等待前一个锁
  private String myZnode; // 当前锁
  private CountDownLatch latch; // 计数器
  private CountDownLatch connectedSignal = new CountDownLatch(1);
  private int sessionTimeout = 30000;

  /**
   * 创建分布式锁,使用前请确认config配置的zookeeper服务可用
   * @param config   localhost:2181
   * @param lockName 竞争资源标志,lockName中不能包含单词_lock_
   */
  public DistributedLock(String config, String lockName) {
    this.lockName = lockName;
    // 创建一个与服务器的连接
    try {
      zk = new ZooKeeper(config, sessionTimeout, this);
      connectedSignal.await();
      Stat stat = zk.exists(root, false); // 此去不执行 Watcher
      if (stat == null) {
        // 创建根节点
        zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // zookeeper节点的监视器
  @Override
  public void process(WatchedEvent event) {
    // 建立连接用
    if (event.getState() == Event.KeeperState.SyncConnected) {
      connectedSignal.countDown();
      return;
    }
    // 其他线程放弃锁的标志
    if (this.latch != null) {
      this.latch.countDown();
    }
  }

  @Override
  public void lock() {
    try {
      if (this.tryLock()) {
        System.out.println("Thread " + Thread.currentThread().getId() + " " + myZnode + " get lock true");
      } else {
        waitForLock(waitNode, sessionTimeout); // 等待锁
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean tryLock() {
    try {
      String splitStr = "_lock_";
      if (lockName.contains(splitStr)) {
        throw new RuntimeException("lockName can not contains \\u000B");
      }
      // 创建临时子节点
      myZnode = zk.create(root + "/" + lockName + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
      System.out.println(myZnode + " is created ");
      // 取出所有子节点
      List<String> subNodes = zk.getChildren(root, false);
      // 取出所有lockName的锁
      List<String> lockObjNodes = new ArrayList<String>();
      for (String node : subNodes) {
        String _node = node.split(splitStr)[0];
        if (_node.equals(lockName)) {
          lockObjNodes.add(node);
        }
      }
      Collections.sort(lockObjNodes);

      if (myZnode.equals(root + "/" + lockObjNodes.get(0))) {
        // 如果是最小的节点,则表示取得锁
        System.out.println(myZnode + "==" + lockObjNodes.get(0));
        return true;
      }
      // 如果不是最小的节点，找到比自己小1的节点
      String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
      waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1); // 找到前一个子节点
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) {
    try {
      if (this.tryLock()) {
        return true;
      }
      return waitForLock(waitNode, time);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean waitForLock(String lower, long waitTime) throws InterruptedException, KeeperException {
    Stat stat = zk.exists(root + "/" + lower, true); // 同时注册监听。
    // 判断比自己小一个数的节点是否存在,如果不存在则无需等待锁,同时注册监听
    if (stat != null) {
      System.out.println("Thread " + Thread.currentThread().getId() + " waiting for " + root + "/" + lower);
      this.latch = new CountDownLatch(1);
      this.latch.await(waitTime, TimeUnit.MILLISECONDS); // 等待，这里应该一直等待其他线程释放锁
      this.latch = null;
    }
    return true;
  }

  @Override
  public void unlock() {
    try {
      System.out.println("unlock " + myZnode);
      zk.delete(myZnode, -1);
      myZnode = null;
      zk.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    this.lock();
  }

  @Override
  public Condition newCondition() {
    return null;
  }
}

```

2. 改造 HelloController.java：

```java
@RestController
public class HelloController {
  @RequestMapping("index")
  public String index() throws Exception {
    DistributedLock lock = new DistributedLock("localhost:2181", "lock");
    lock.lock();
    // 共享资源
    if(lock != null) {
      System.out.println("执行方法");
      Thread.sleep(5000);
      lock.unlock();
    }
    return "hello world!";
  }
}
```

3. 按照本文 Redis 分布式锁的方法测试，我们发现同样成功加锁了。

### 总结

通过以上的实例可以得出以下结论：

- 通过数据库实现分布式锁是最不可靠的一种方式，对数据库依赖较大，性能较低，不利于处理高并发的场景。
- 通过 Redis 的 Redlock 和 ZooKeeper 来加锁，性能有了比较大的提升。
- 针对 Redlock，曾经有位大神对其实现的分布式锁提出了质疑，但是 Redis 官方却不认可其说法，所谓公说公有理婆说婆有理，对于分布式锁的解决方案，没有最好，只有最适合的，根据不同的项目采取不同方案才是最合理的。