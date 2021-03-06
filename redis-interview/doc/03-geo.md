# 如何实现查询附近的人？

- [典型回答](#典型回答)
- [考点分析](#考点分析)
- [知识扩展](#知识扩展)
  - [经纬度获取](#经纬度获取)
  - [用代码实现查询附近的人](#用代码实现查询附近的人)
  - [GEO 类型底层实现](#geo-类型底层实现)
- [总结](#总结)

查询附近的人或者是附近的商家是一个实用且常用的功能，比如微信中“附近的人”或是美团外卖中“附近商家”等，本文就一起来看它是如何实现的

本文的面试题是：使用 Redis 如何实现查询附近的人？

### 典型回答

在说如何实现地理位置查询之前，首先需要搞清楚地理位置查询的基础知识：经度和纬度。

任何位置都可以用经度和纬度来标识，经度的范围 -180 到 180，纬度的范围为：-90 到 90。纬度以赤道为界，赤道以南为负数，赤道以北为正数；经度以本初子午线 (英国格林尼治天文台) 为界，东边为正数，西边为负数。这样位置才能在地球上被标注出来，也成为了能够查询出两点之间距离的基础。

从而让查询附近的人变得简单了，只需要查询出附近几个点和自己的距离，再进行排序就可以实现“查询附近的人”的功能了，然而使用 Redis 让这一切更简单了，Redis 提供了专门用来存储地理位置的类型 GEO，使用它以及它的内置方法就可以轻松的实现“查询附近的人”了。

可以使用 Redis 3.2 版本中新增的 GEO 类型，以及它的 georadius 命令来实现查询附近的人。先添加几个人的位置信息，实现命令如下：

```shell
> geoadd site 116.404269 39.913164 tianan
(integer) 1
> geoadd site 116.36 39.922461 yuetan
(integer) 1
> geoadd site 116.499705 39.874635 huanle
(integer) 1
> geoadd site 116.193275 39.996348 xiangshan
(integer) 1
```

使用 `geoadd` 命令**添加位置信息**，语法为：`geoadd key longitude latitude member [longitude latitude member ...]` 此命令支持一次添加一个或多个位置信息，其中：

- longitude 表示经度
- latitude 表示纬度
- member 是为此经纬度起的名字

在查询某个人（某个经纬度）附近的人，实现命令如下:

```shell
> georadius site 116.405419 39.913164 5 km
1) "tianan"
2) "yuetan"
```

从上述结果中可以看出在经纬度为 116.405419,39.913164 的附近五公里范围内有两个人“tianan”和“yuetan”，于是查询附近人的功能就算实现完成了。

使用 `georadius` 命令**查询附近的人**，语法为： `georadius key longitude latitude radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC]`

georadius 命令可以使用单位：m米、km千米、mi英里、ft英尺

georadius 命令可选参数：WITHCOORD、WITHDIST、WITHHASH、COUNT count、ASC|DESC

- WITHCOORD：返回满足条件位置的经纬度信息coordinate

  ```shell
  > georadius site 116.405419 39.913164 5 km withcoord
  1) 1) "tianan"
     2) 1) "116.40426903963088989"
        2) "39.91316289865137179"
  2) 1) "yuetan"
     2) 1) "116.36000186204910278"
        2) "39.92246025586381819"
  ```

- WITHDIST：返回满足条件位置与查询位置的直线距离distance

  ```shell
  > georadius site 116.405419 39.913164 5 km withdist
  1) 1) "tianan"
     2) "0.0981"
  2) 1) "yuetan"
     2) "4.0100"
  ```

- WITHHASH：返回满足条件位置的哈希信息
  
  ```shell
  > georadius site 116.405419 39.913164 5 km withhash
  1) 1) "tianan"
     2) (integer) 4069885552230465
  2) 1) "yuetan"
     2) (integer) 4069879797297521
  ```
  
- COUNT count：指定返回满足条件位置的个数。 如返回一条满足条件的信息
  
  ```shell
  > georadius site 116.405419 39.913164 5 km count 1
  1) "tianan"
  ```
  
- ASC|DESC：从近到远|从远到近排序返回

  ```shell
  > georadius site 116.405419 39.913164 5 km desc
  1) "yuetan"
  2) "tianan"
  > georadius site 116.405419 39.913164 5 km asc
  1) "tianan"
  2) "yuetan"
  ```

当然以上这些可选参数也可以一起使用，例如以下代码：

```shell
> georadius site 116.405419 39.913164 5 km withdist desc
1) 1) "yuetan"
   2) "4.0100"
2) 1) "tianan"
   2) "0.0981"
```

### 考点分析

查询附近的人看似是一个复杂的问题，但借助 Redis 还是很好实现的。和此知识点相关的面试题还有以下这些：

- 如何查询位置的经纬度信息？
- 如何在代码中实现查询附近的人？
- GEO 类型的底层是如何实现的？

### 知识扩展

#### 经纬度获取

借助在线坐标查询系统来获取经纬度的值，例如百度的坐标系统：http://api.map.baidu.com/lbsapi/getpoint/index.html

可以在搜索栏输入位置信息，然后使用鼠标点击该区域就会出现经纬度信息

#### 用代码实现查询附近的人

使用 Java 语言来实现“查询附近的人”，借助 Jedis 包来操作 Redis，在 pom.xml 添加 Jedis 框架的引用，配置如下：

```xml
<dependency>
  <groupId>redis.clients</groupId>
  <artifactId>jedis</artifactId>
  <version>3.3.0</version>
</dependency>
```

实现代码 GeoHashExample.java 如下：

```java
Map<String, GeoCoordinate> map = new HashMap<>();
map.put("xiaoming", new GeoCoordinate(116.404269, 39.913164)); // 添加小明的位置
map.put("xiaohong", new GeoCoordinate(116.36, 39.922461)); // 添加小红的位置
map.put("xiaomei", new GeoCoordinate(116.499705, 39.874635)); // 添加小美的位置
map.put("xiaoer", new GeoCoordinate(116.193275, 39.996348)); // 添加小二的位置
Jedis jedis = new Jedis("127.0.0.1", 6379);
jedis.geoadd("person", map);

// 查询小明和小红的直线距离
System.out.println("小明和小红相距：" + jedis.geodist("person", "xiaoming", "xiaohong", GeoUnit.KM) + " KM");
// 查询小明附近 5 公里的人
List<GeoRadiusResponse> res = jedis.georadiusByMemberReadonly("person", "xiaoming", 5, GeoUnit.KM);
res.forEach(e -> System.out.println("小明附近的人：" + e.getMemberByString()));
```

以上程序执行的结果如下：

```
小明和小红相距：3.9153 KM
小明附近的人：xiaoming
小明附近的人：xiaohong
```

#### GEO 类型底层实现

GEO 类型主要的命令如下：

```shell
geoadd # 添加地理位置
georadius # 查询某位置内的其他成员信息
geodist # 距离统计
geopos # 查询位置信息
geohash # 查询位置的哈希值
```

很神奇的发现竟然没有删除命令，于是打开 GEO 的源码才发现 **GEO 类型的底层是 ZSet（有序集合）实现**的，因此可以使用 zrem 命令来删除地理位置信息。GEO 实现的主要源码为：

```c++
void geoaddCommand(client *c) {
  // 参数校验
  if ((c->argc - 2) % 3 != 0) {
    /* Need an odd number of arguments if we got this far... */
    addReplyError(c, "syntax error. Try GEOADD key [x1] [y1] [name1] "
                  "[x2] [y2] [name2] ... ");
    return;
  }
  // 参数提取 Redis
  int elements = (c->argc - 2) / 3;
  int argc = 2+elements*2; /* ZADD key score ele ... */
  robj **argv = zcalloc(argc*sizeof(robj*));
  argv[0] = createRawStringObject("zadd",4);
  argv[1] = c->argv[1]; /* key */
  incrRefCount(argv[1]);
  // 参数遍历+转换
  int i;
  for (i = 0; i < elements; i++) {
    double xy[2];
    // 提取经纬度
    if (extractLongLatOrReply(c, (c->argv+2)+(i*3),xy) == C_ERR) {
      for (i = 0; i < argc; i++)
        if (argv[i]) decrRefCount(argv[i]);
      zfree(argv);
      return;
    }
    // 将经纬度转换为 52 位的 geohash 作为分值 & 提取对象名称
    GeoHashBits hash;
    geohashEncodeWGS84(xy[0], xy[1], GEO_STEP_MAX, &hash);
    GeoHashFix52Bits bits = geohashAlign52Bits(hash);
    robj *score = createObject(OBJ_STRING, sdsfromlonglong(bits));
    robj *val = c->argv[2 + i * 3 + 2];
    // 设置有序集合的对象元素名称和分值
    argv[2+i*2] = score;
    argv[3+i*2] = val;
    incrRefCount(val);
  }
  replaceClientCommandVector(c,argc,argv);
  // 调用 zadd 命令，存储转化好的对象
  zaddCommand(c);
}
```

通过上述源码可以看出 Redis 内部使用 ZSet 来保存位置对象的，它使用 ZSet 的 Score 来存储经纬度对应的 52 位的 GEOHASH 值的。

### 总结

本文讲了使用 Redis 实现查询附近的人的实现方案，既使用 Redis 3.2 新增的 GEO 类型的 georadius 命令来实现，还是用 Java 代码演示了查询附近的人，并讲了 GEO 其他的几个命令的使用，以及经纬度的获取方法和 GEO 底层的实现。