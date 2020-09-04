# Redis 面试题补充与汇总

前面的 12 个章节对 Redis 的面试题做了系统的讲解，本文将对 Redis 的热门面试题再做一个补充，力求覆盖到更多的 Redis 面试点。

### Redis 持久化

Redis 持久化总共有以下三种方式：

- **快照方式**（RDB, Redis DataBase）将某一个时刻的内存数据，以二进制的方式写入磁盘
- **追加方式**（AOF, Append Only File）记录所有的操作命令，并以文本的形式追加到文件中
- **混合方式**，Redis 4.0 之后新增的方式，混合持久化是结合了 RDB 和 AOF 的优点，在写入的时候，先把当前的数据以 RDB 的形式写入文件的开头，再将后续的操作命令以 AOF 的格式存入文件，这样既能保证 Redis 重启时的速度，又能降低数据丢失的风险。

#### 1.RDB 持久化

RDB（Redis DataBase）是将某一个时刻的内存快照以二进制的方式写入磁盘的过程。它的持久化触发方式有两类：手动触发、自动触发。

##### 手动持久化

手动触发持久化的操作有两个： `save` 和 `bgsave` 。它们的区别在于使用 `bgsave` 不会阻塞 Redis 主线程的业务执行。

1. save 执行流程。如下图所示：

   <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t15o088j30c80fqaa4.jpg" alt="save 执行流程.jpg" width="190" />

2. bgsave 执行流程。如下图所示：

   <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t18uvmxj30fm0nkq39.jpg" alt="bgsave 执行流程.jpg" width="250" />

##### 自动持久化

1. save m n

   `save m n` 是指在 m 秒内，如果有 n 个键发生改变，则自动触发持久化。 参数 m 和 n 可以在 Redis 的配置文件中找到，例如，`save 60 1` 则表明在 60 秒内，至少有一个键发生改变，就会触发 RDB 持久化。 自动触发持久化的本质是 Redis 通过判断，如果满足设置的触发条件，就会自动执行一次 `bgsave` 命令。 

   > 注意：当设置多个 save m n 命令时，满足任意一个条件都会触发持久化。 例如，设置以下两个 save m n 命令：

   ```shell
   save 60 10
   save 600 1
   ```

   当 60s 内如果有 10 次 Redis 键值发生改变，就会触发持久化；如果 60s 内 Redis 的键值改变次数少于 10 次，那么 Redis 就会判断 600s 内 Redis 的键值是否至少被修改了一次，如果满足则会触发持久化。
   
2. flushall

   flushall 命令用于清空 Redis 数据库，在生产环境下一定慎用，当 Redis 执行了 flushall 命令之后，则会触发自动持久化，把 RDB 文件清空。 执行结果如下所示：

   ```shell
   > flushall
   OK
   ```

3. 主从同步触发

   在 Redis 主从复制中，当从节点执行全量复制操作时，主节点会执行 `bgsave` 命令，并将 RDB 文件发送给从节点，该过程会自动触发 Redis 持久化。

##### RDB 优点

- RDB 的内容为二进制的数据，占用内存更小，更紧凑，更适合做为备份文件；
- RDB 对灾难恢复非常有用，它是一个紧凑的文件，可以更快的传输到远程服务器进行 Redis 服务恢复；
- RDB 可以更大程度的提高 Redis 的运行速度，因为每次持久化时 Redis 主进程都会 fork() 一个子进程，进行数据持久化到磁盘，Redis 主进程并不会执行磁盘 I/O 等操作；
- 与 AOF 格式的文件相比，RDB 文件可以更快的重启。

##### RDB 缺点如下

- 因为 RDB 只能保存某个时间间隔的数据，如果中途 Redis 服务被意外终止了，则会丢失一段时间内的 Redis 数据；
- RDB 需要经常 fork() 才能使用子进程将其持久化在磁盘上。如果数据集很大，fork() 可能很耗时，并且如果数据集很大且 CPU 性能不佳，则可能导致 Redis 停止为客户端服务几毫秒甚至一秒钟。

#### 2.AOF 持久化

AOF（Append Only File）附加到文件， AOF 可以把 Redis 每个键值对操作都记录到文件（appendonly.aof）中。

Redis 默认是关闭 AOF 持久化的，想要开启 AOF 持久化有以下两种方式：

- 通过命令行的方式

  ```shell
  > config set appendonly yes
  OK
  > config get appendonly
  1) "appendonly"
  2) "yes"
  ```

  优点：无需重启 Redis 服务

  缺点：Redis 服务重启，则命令行设置将会失效

- 通过修改配置文件的方式（redis.conf）

  ```shell
  appendonly yes # 默认值为 no，表示关闭 AOF 持久化
  ```

  优点：无论重启多少次 Redis 服务，配置文件中的配置信息都不会失效

  缺点：每次修改配置文件都要重启 Redis 服务才能生效

##### AOF 优点

- AOF 持久化保存的数据更加完整，AOF 提供了三种保存策略：**每次操作保存**、**每秒钟保存**、**跟随系统的持久化策略保存**

  其中每秒钟保存，从数据的安全性和性能两方面考虑是个不错的选择，也是 AOF 默认的策略，即使发生了意外情况，最多只会丢失 1s 钟的数据

- AOF 采用的是**命令追加**的写入方式，所以不会出现文件损坏的问题，即使由于某些意外原因导致了最后操作的持久化数据写入了一半，也可以通过 redis-check-aof 工具轻松的修复

- AOF 持久化文件非常容易理解和解析，它是把所有 Redis 键值操作命令以文件的方式存入了磁盘。即使不小心使用 `flushall` 命令删除了所有键值信息，只要使用 AOF 文件删除最后的 `flushall` 命令，重启 Redis 即可恢复之前误删的数据。

##### AOF 缺点

- 对于相同的数据集来说，AOF 文件要大于 RDB 文件
- 在 Redis 负载比较高的情况下，RDB 比 AOF 性能更好
- RDB 使用快照的形式来持久化整个 Redis 数据，而 AOF 只是将每次执行的命令追加到 AOF 文件中，因此从理论上说 RDB 比 AOF 更健壮

#### 3.混合持久化

RDB 和 AOF 持久化各有利弊：RDB 可能会导致一定时间内的数据丢失，而 AOF 由于文件较大则会影响 Redis 的启动速度，为了能同时使用 RDB 和 AOF 各种的优点，Redis 4.0 之后新增了混合持久化的方式。

在开启混合持久化的情况下，AOF 重写时会把 Redis 的持久化数据，以 RDB 的格式写入到 AOF 文件的开头，之后的数据再以 AOF 的格式追加到文件末尾。

混合持久化的数据存储结构如下图所示：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t19ypkdj30dk0fct8s.jpg" alt="混合持久化的数据存储结构.jpg" style="zoom:50%;" />

查询混合持久化是否开启，执行结果如下所示：

```shell
> config get aof-use-rdb-preamble # 查询混合持久化是否开启
1) "aof-use-rdb-preamble"
2) "yes"
```

其中 yes 表示已经开启混合持久化，no 表示关闭。Redis 5.0 默认值为 yes。

##### 混合持久化优点

- 混合持久化结合了 RDB 和 AOF 持久化的优点，开头为 RDB 的格式使得 Redis 可以更快的启动，同时结合 AOF 的优点又降低了数据丢失的风险

##### 混合持久化缺点

- AOF 文件中添加了 RDB 格式的内容，使得 AOF 文件的可读性变差
- 兼容性差，如果开启混合持久化，那么此混合持久化 AOF 文件，就不能用在 Redis 4.0 之前版本了

### 缓存雪崩

缓存雪崩是指在短时间内，有大量缓存同时过期，导致大量的请求直接查询数据库，从而对数据库造成了巨大的压力，严重情况下可能会导致数据库宕机的情况叫做缓存雪崩。先看下正常情况和缓存雪崩时程序的执行流程图：

正常情况系统的执行流程，如下图所示：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t18gieij30hx03pt8m.jpg" alt="正常访问.jpg" style="zoom:80%;" />

缓存雪崩的执行流程，如下图所示： 

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t16adk9j30e506y3yi.jpg" alt="缓存雪崩.jpg" style="zoom:80%;" />

以上对比图可以看出缓存雪崩对系统造成的影响，那如何解决缓存雪崩的问题？ 缓存雪崩的**常用解决方案**有以下几个：

1. **加锁排队**

   加锁排队可以起到缓冲的作用，防止大量请求同时操作数据库。但它的缺点是增加了系统的响应时间，降低了系统的吞吐量，牺牲了用户体验。 加锁排队的实现代码如下所示：

   ```java
   String cacheKey = "userlist"; // 缓存 key
   String data = jedis.get(cacheKey); // 查询缓存
   if (StringUtils.isNotBlank(data)) {
     // 查询到数据，直接返回结果
     return data;
   } else {
     // 先排队查询数据库，在放入缓存
     synchronized (cacheKey) {
       data = jedis.get(cacheKey);
       if (StringUtils.isBlank(data)) { // 双重判断
         data = findUserInfo(); // 查询数据库
         jedis.set(cacheKey, data); // 放入缓存
       }
       return data;
     }
   }
   ```

2. **随机化过期时间**

   为避免缓存同时过期，可在设置缓存时添加随机时间，这样就可以极大的避免大量的缓存同时失效。 示例代码如下：

   ```java
   int exTime = 10 * 60; // 缓存原本的失效时间
   Random random = new Random(); // 随机数生成类
   // 缓存设置
   jedis.setex(cacheKey, exTime+random.nextInt(1000), value);
   ```

3. **设置二级缓存**

   二级缓存指的是除了 Redis 本身的缓存，再设置一层缓存，当 Redis 失效之后，先去查询二级缓存。 例如可以设置一个本地缓存，在 Redis 缓存失效的时候先去查询本地缓存而非查询数据库。 加入二级缓存之后程序执行流程，如下图所示：

   <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t17j6ssj30ma0ffmxm.jpg" alt="二级缓存.jpg" style="zoom:50%;" />

### 缓存穿透

缓存穿透是指查询缓存和数据库都无数据，因为数据库查询无数据，出于容错考虑，不会将结果保存到缓存中，因此每次请求都会去查询数据库。 缓存穿透执行流程如下图所示：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t17ymsxj307p0eq0sl.jpg" alt="缓存穿透.jpg" style="zoom:80%;" />

其中红色路径表示缓存穿透的执行路径，可以看出缓存穿透会给数据库造成很大的压力。 缓存穿透的解决方案有以下几个：

1. 使用过滤器

   使用过滤器来减少对数据库的请求。例如使用布隆过滤器，它的原理是将数据库的数据哈希到 bitmap 中，每次查询之前，先使用布隆过滤器过滤掉一定不存在的无效请求，从而避免了无效请求给数据库带来的查询压力。

2. **缓存空结果**

   把每次从数据库中查询的数据都保存到缓存，为了提高前台用户的使用体验 (解决长时间内查询不到任何信息的情况)，可以将空结果的缓存时间设置的短一些，例如 3-5 分钟。

### 缓存击穿

缓存击穿指的是某个热点缓存，在某一时刻恰好失效了，然后此时刚好有大量的并发请求，此时这些请求将会给数据库造成巨大的压力。 缓存击穿的执行流程如下图所示：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t13tlqhj30jj09qdg0.jpg" alt="缓存击穿.jpg" style="zoom:60%;" />

缓存击穿的解决方案有以下 2 个：

1. **加锁排队**

   此处理方式和缓存雪崩加锁排队的方法类似，都是**在查询数据库时加锁排队**，缓冲操作请求以此来减少服务器的运行压力

2. **设置永不过期**

   对**热点缓存设置永不过期**，这样就能保证缓存的稳定性，但注意需要在数据更改之后，要**及时更新热点缓存**，不然就会造成查询结果的误差

### 缓存预热

首先缓存预热并不是一个问题，而是使用缓存时的一个优化方案，它可以提高前台用户的使用体验。 **缓存预热指的是在系统启动的时候，先把查询结果预存到缓存中，以便用户后面查询时可以直接从缓存中读取，以节约用户的等待时间**。缓存预热的执行流程如下图所示：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gi9t1acxjuj30rc0dfq3d.jpg" alt="缓存预热.jpg" style="zoom:60%;" />

缓存预热的实现思路有以下三种：

1. 把需要缓存的方法写在**系统初始化的方法**中，这样系统在启动的时候就会自动加载并缓存数据
2. 把需要缓存的方法挂载到**某个页面或后端接口**上，手动触发缓存预热
3. 设置**定时任务**自动进行缓存预热

