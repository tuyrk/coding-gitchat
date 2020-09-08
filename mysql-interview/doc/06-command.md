# MySQL 命令和内置函数

- [1. 如何用命令行方式连接 MySQL 数据库？](#1-如何用命令行方式连接-mysql-数据库)
- [2. 关于命令 `mysql -h 127.0.0.1 -P 3307 -uroot -p3307` 以下说法错误的是？](#2-关于命令-mysql--h-127001--p-3307--uroot--p3307-以下说法错误的是)
- [3. 如何创建用户？并给用户授权？](#3-如何创建用户并给用户授权)
- [4. 如何修改 MySQL 密码？](#4-如何修改-mysql-密码)
- [5. 如何使用 SQL 创建数据库，并设置数据库的编码格式？](#5-如何使用-sql-创建数据库并设置数据库的编码格式)
- [6. 如何修改数据库、表的编码格式？](#6-如何修改数据库表的编码格式)
- [7. 如何使用 SQL 创建表？](#7-如何使用-sql-创建表)
- [8. 在 MySQL 命令行中如何查看表结构信息？](#8-在-mysql-命令行中如何查看表结构信息)
- [9. 如何使用 SQL 查看已知表的建表脚本？](#9-如何使用-sql-查看已知表的建表脚本)
- [10. 如何使用 SQL 语句更新表结构？](#10-如何使用-sql-语句更新表结构)
- [11. MySQL 有哪些删除方式？有什么区别？](#11-mysql-有哪些删除方式有什么区别)
- [12. 如何开启和关闭 MySQL 服务？](#12-如何开启和关闭-mysql-服务)
- [13. 如何查询当前 MySQL 安装的版本号？](#13-如何查询当前-mysql-安装的版本号)
- [14. 如何查看某张表的存储引擎？](#14-如何查看某张表的存储引擎)
- [15. 如何查看当前数据库增删改查的执行次数统计？](#15-如何查看当前数据库增删改查的执行次数统计)
- [16. 如何查询线程连接数？](#16-如何查询线程连接数)
- [17. 如何查看 MySQL 的最大连接数？能不能修改？怎么修改？](#17-如何查看-mysql-的最大连接数能不能修改怎么修改)
- [18. CHAR_LENGTH 和 LENGTH 有什么区别？](#18-char_length-和-length-有什么区别)
- [19. UNION 和 UNION ALL 的用途是什么？有什么区别？](#19-union-和-union-all-的用途是什么有什么区别)
- [20. 以下关于 WHERE 和 HAVING 说法正确的是？](#20-以下关于-where-和-having-说法正确的是)
- [21. 空值和 NULL 的区别是什么？](#21-空值和-null-的区别是什么)
- [22. MySQL 的常用函数有哪些？](#22-mysql-的常用函数有哪些)

#### 1. 如何用命令行方式连接 MySQL 数据库？

使用 `mysql -u用户名 -p密码;` 输入用户名和密码就可以正常进入数据库连接了，实例如下：

```mysql
mysql -uroot -p123456
```

其中，用户名为 root，密码为 123456

#### 2. 关于命令 `mysql -h 127.0.0.1 -P 3307 -uroot -p3307` 以下说法错误的是？

> A：-h 和 -P 可以省略
>
> B：-u 和用户名之间不能有空格
>
> C：-p 和密码之间不能用空格
>
> D：小写 -p 对应的是用户密码，大写 -P 对应的是 MySQL 服务器的端口

答：B

题目解析：-p 和密码之间不能用空格，否则空格会被识别为密码的一部分，提示密码错误。-u 和用户名之间可以有空格。

#### 3. 如何创建用户？并给用户授权？

创建用户使用关键字：`CREATE USER` ，授权使用关键字： `GRANT` ，具体实现脚本如下：

```mysql
-- 创建用户 laowang
create user 'laowang'@'localhost' identified by '123456';
-- 授权 test 数据库给 laowang
grant all on test.* to 'laowang'@'localhost';
```

#### 4. 如何修改 MySQL 密码？

使用如下命令，修改密码：

```shell
mysqladmin -u用户名 -p旧密码 password 新密码;
```

注意：刚开始 root 没有密码，所以 `-p旧密码`一项就可以省略

#### 5. 如何使用 SQL 创建数据库，并设置数据库的编码格式？

创建数据库可使用关键字： `CREATE DATABASE` ，设置编码格式使用关键字： `CHARSET` ，具体 SQL 如下：

```mysql
create database dbname
    default charset utf8
    collate utf8_general_ci;
```

#### 6. 如何修改数据库、表的编码格式？

使用 `alter` 关键字设置库或表的编码格式即可，具体代码如下：

```mysql
alter database dbname
    default character set utf8;
alter table t
    default character set utf8;
```

#### 7. 如何使用 SQL 创建表？

```mysql
create table t (
  t_id   int      not null auto_increment,
  t_name char(50) not null,
  t_age  int      null default 18,
  primary key (t_id)
) engine = innodb;
```

其中：

- auto_increment：表示自增
- primary key：用于指定主键
- engine：用于指定表的引擎

#### 8. 在 MySQL 命令行中如何查看表结构信息？

使用 `desc 表名` 查看表结构信息，示例信息如下：

```mysql
desc t; # 查看表 t 的结构信息
```

```powershell
+--------+----------+------+-----+---------+----------------+
| Field  | Type     | Null | Key | Default | Extra          |
+--------+----------+------+-----+---------+----------------+
| t_id   | int      | NO   | PRI | NULL    | auto_increment |
| t_name | char(50) | NO   |     | NULL    |                |
| t_age  | int      | YES  |     | 18      |                |
+--------+----------+------+-----+---------+----------------+
```

#### 9. 如何使用 SQL 查看已知表的建表脚本？

使用`show create table 表名`查看已知表的建表脚本

```mysql
show create table t;
```

```
+-------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Table | Create Table                                                                                                                                                                   |
+-------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| t     | CREATE TABLE `t` (
  `t_id` int NOT NULL AUTO_INCREMENT,
  `t_name` char(50) NOT NULL,
  `t_age` int DEFAULT '18',
  PRIMARY KEY (`t_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 |
+-------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

#### 10. 如何使用 SQL 语句更新表结构？

更新表结构信息可以使用 alter table 子句。

表增加列

```mysql
alter table t
    add name char(20);
```

重命名列

```mysql
alter table t
    rename column height to t_height;
```

重命名表

```mysql
rename table t to new_t;
```

#### 11. MySQL 有哪些删除方式？有什么区别？

MySQL 有三种删除方式： 

1）删除表数据：

```mysql
delete from t;
```

2）删除数据，保留表结构：

```mysql
truncate table t;
```

3）删数据和表结构：

```mysql
drop table t;
```

它们的区别如下：

- delete：可以有条件的删除，也可以回滚数据，删除数据时进行两个动作：删除与备份，所以速度很慢
- truncate：删除所有数据，无条件选择删除，不可回滚，保留表结构
- drop：删除数据和表结构 删除速度最快

#### 12. 如何开启和关闭 MySQL 服务？

使用 `systemctl start mysqld` 启动 MySQL 服务

使用 `systemctl stop mysqld` 停止 MySQL 服务

#### 13. 如何查询当前 MySQL 安装的版本号？

- 控制台命令

  ```shell
mysql --version
  ```
  
  ```
mysql  Ver 8.0.19 for Linux on x86_64 (MySQL Community Server - GPL)
  ```
  
- SQL查询

  ```mysql
  select version();
  ```

  ```
  8.0.19
  ```

#### 14. 如何查看某张表的存储引擎？

查询数据库 db 中表 t 的所有信息，其中 `Engine` 列表示表 t 使用的存储引擎

```mysql
show table status from dbname where name = 'tname';
```

```powershell
+-------+--------+---------+------------+------+----------------+-------------+-----------------+--------------+-----------+----------------+---------------------+-------------+------------+-----------------+----------+----------------+---------+
| Name  | Engine | Version | Row_format | Rows | Avg_row_length | Data_length | Max_data_length | Index_length | Data_free | Auto_increment | Create_time         | Update_time | Check_time | Collation       | Checksum | Create_options | Comment |
+-------+--------+---------+------------+------+----------------+-------------+-----------------+--------------+-----------+----------------+---------------------+-------------+------------+-----------------+----------+----------------+---------+
| tname | InnoDB |      10 | Dynamic    |    0 |              0 |       16384 |               0 |            0 |         0 |              1 | 2020-08-11 17:18:55 | NULL        | NULL       | utf8_general_ci |     NULL |                |         |
+-------+--------+---------+------------+------+----------------+-------------+-----------------+--------------+-----------+----------------+---------------------+-------------+------------+-----------------+----------+----------------+---------+
```

#### 15. 如何查看当前数据库增删改查的执行次数统计？

```mysql
show global status
    where variable_name in ('com_select', 'com_insert', 'com_delete', 'com_update');
```

```powershell
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| Com_delete    | 0     |
| Com_insert    | 0     |
| Com_select    | 509   |
| Com_update    | 0     |
+---------------+-------+
```

#### 16. 如何查询线程连接数？

```mysql
show global status where variable_name like 'threads_%';
```

```powershell
+-------------------+-------+
| Variable_name     | Value |
+-------------------+-------+
| Threads_cached    | 2     | # 当前此时此刻线程缓存中有多少空闲线程
| Threads_connected | 3     | # 当前已建立连接的数量，因为一个连接就需要一个线程，所以也可以看成当前被使用的线程数
| Threads_created   | 5     | # 从最近一次服务启动，已创建线程的数量
| Threads_running   | 2     | # 当前激活的（非睡眠状态）线程数
+-------------------+-------+
```

#### 17. 如何查看 MySQL 的最大连接数？能不能修改？怎么修改？

```mysql
show variables like 'max_connections';
```

```powershell
+-----------------+-------+
| Variable_name   | Value |
+-----------------+-------+
| max_connections | 151   |
+-----------------+-------+
```

可以修改 MySQL 的最大连接数，可以在 MySQL 的配置文件 my.cnf 里修改最大连接数，通过修改 max_connections 的值，然后重启 MySQL 就会生效，如果 my.ini 文件中没有找到 max_connections，可自行添加 max_connections 的设置，内容如下：

```
max_connections=200
```

#### 18. CHAR_LENGTH 和 LENGTH 有什么区别？

CHAR_LENGTH 是字符数，而 LENGTH 是字节数。它们在不同编码下，值是不相同的，比如对于 UTF-8 编码来说，一个中文字的 CHAR_LENGTH 为 1，而 LENGTH 通常等于 3，如下图所示：

```mysql
select char_length('中'), length('中');
```

```powershell
+-----------------+------------+
| char_length('') | length('') |
+-----------------+------------+
|               1 |          3 |
+-----------------+------------+
```

#### 19. UNION 和 UNION ALL 的用途是什么？有什么区别？

UNION 和 UNION ALL 都是用于合并数据集的，它们的区别如下：

- 去重：UNION 会对结果进行去重，UNION ALL 则不会进行去重操作
- 排序：UNION 会对结果根据字段进行排序，而 UNION ALL 则不会进行排序
- 性能：UNION ALL 的性能要高于 UNION

#### 20. 以下关于 WHERE 和 HAVING 说法正确的是？

> A：任何情况 WHERE 和 HAVING 都可以相互替代
>
> B：GROUP BY 前后都可以使用 WHERE
>
> C：使用 SELECT X FROM T HAVING Y>20 查询报错
>
> D：使用 SELECT X FROM T WHERE Y>20 查询报错

答：C

题目解析：HAVING 非报错用法是 `SELECT X,Y FROM T HAVING Y>20` 。

#### 21. 空值和 NULL 的区别是什么？

空值表示字段的值为空，而 NULL 则表示字段没有值，它们的区别如下：

- 空值不占用空间，NULL 值是未知的占用空间
- 空值判断使用 `=''` 或 `<>''` 来判断，NULL 值使用 `IS NULL` 或 `IS NOT NULL` 来判断
- 使用 COUNT 统计某字段时，如果是空值则会算入统计之内，而 NULL 则会忽略不统计

比如，其中字段 `name` 有两个 `NULL` 值和一个空值，查询结果如图：

```mysql
select count(*), count(name) from person;
```

```powershell
+----------+-------------+
| count(*) | count(name) |
+----------+-------------+
|        3 |           1 |
+----------+-------------+
```

#### 22. MySQL 的常用函数有哪些？

```mysql
sum(field) # 求某个字段的和值
count(*) # 查询总条数
max(field) # 某列中最大的值
min(field) # 某列中最小的值
avg(field) # 求平均数

current_date() # 获取当前日期
now() # 获取当前日期和时间
concat(a, b) # 连接两个字符串值以创建单个字符串输出
datediff(a, b) # 确定两个日期之间的差异，通常用于计算年龄
```

```mysql
select current_date(), now(), concat('123', '456'), datediff('2020-08-13 14:53:49', '2020-09-13');
```

```powershell
+----------------+---------------------+----------------------+-----------------------------------------------+
| current_date() | now()               | concat('123', '456') | datediff('2020-08-13 14:53:49', '2020-09-13') |
+----------------+---------------------+----------------------+-----------------------------------------------+
| 2020-08-13     | 2020-08-13 14:55:48 | 123456               |                                           -31 |
+----------------+---------------------+----------------------+-----------------------------------------------+
```
