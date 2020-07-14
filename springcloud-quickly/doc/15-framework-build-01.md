# 第14课：Spring Cloud 实例详解——基础框架搭建（一）

通过前面基础组件的学习，我们已经可以利用这些组件搭建一个比较完整的微服务架构，为了巩固我们前面学习的知识，从本课开始，将以一个实际的案例带领大家构建一个完整的微服务架构（本课代码已放在 [GitHub](https://github.com/lynnlovemin/SpringCloudInActivity) 上）。

### 需求分析

本课要实现的一个产品是新闻门户网站，首先我们需要对其进行需求分析，本新闻门户网站包括的功能大概有以下几个：

- 注册登录
- 新闻列表
- 用户评论

### 产品设计

> 根据需求分析，就可以进行产品设计，主要是原型设计，我们先看看大致的原型设计图。

- 首页原型设计图
  <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2aaxpf3j31ee0u0akq.jpg" style="zoom: 35%;" />

- 文章列表页原型设计图
  <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2aewe6dj31em0u049b.jpg" style="zoom:35%;" />

- 文章详情页原型设计图
  <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2ac3dgsj31ee0u0n9a.jpg" style="zoom:35%;" />

- 个人中心页原型设计图
  <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2adv7tej31ed0u0dkf.jpg" style="zoom: 35%;" />

- 用户注册页原型设计图
  <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2abndfej31ed0u0gq0.jpg" style="zoom:35%;" />

- 用户登录页原型设计图
  <img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2aecbmvj31ej0u00ws.jpg" style="zoom:35%;" />

### 数据库设计

根据原型设计图，我们可以分析出数据结构，从而设计数据库：

```sql
DROP DATABASE IF EXISTS `blog_db`;
CREATE DATABASE `blog_db`;
use `blog_db`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `news_article`;
CREATE TABLE `news_article` (
    `id`           bigint(16) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `gmt_create`   datetime     DEFAULT NULL COMMENT '创建时间',
    `gmt_modified` datetime     DEFAULT NULL COMMENT '修改时间',
    `title`        varchar(64)  DEFAULT NULL COMMENT '标题',
    `summary`      varchar(256) DEFAULT NULL COMMENT '摘要',
    `pic_url`      varchar(256) DEFAULT NULL COMMENT '图片',
    `view_count`   int(8)       DEFAULT NULL COMMENT '浏览数',
    `source`       varchar(32)  DEFAULT NULL COMMENT '来源',
    `content`      text COMMENT '文章内容',
    `category_id`  bigint(16)   DEFAULT NULL COMMENT '分类ID',
    `is_recommend` tinyint(1)   DEFAULT '0' COMMENT '是否推荐',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '新闻文章';

DROP TABLE IF EXISTS `news_captcha`;
CREATE TABLE `news_captcha` (
    `id`           bigint(16) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `gmt_create`   datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `mobile`       varchar(16) DEFAULT NULL COMMENT '手机号',
    `code`         varchar(8)  DEFAULT NULL COMMENT '验证码',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '新闻验证码';

DROP TABLE IF EXISTS `news_category`;
CREATE TABLE `news_category` (
    `id`           bigint(16) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `gmt_create`   datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `name`         varchar(16)         DEFAULT NULL COMMENT '分类名',
    `parent_id`    bigint(16) NOT NULL DEFAULT '0' COMMENT '上级分类ID（0为顶级分类）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '新闻类别';

DROP TABLE IF EXISTS `news_comment`;
CREATE TABLE `news_comment` (
    `id`           bigint(16) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `gmt_create`   datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `article_id`   bigint(16)          DEFAULT NULL COMMENT '文章ID',
    `content`      varchar(256)        DEFAULT NULL COMMENT '评论内容',
    `parent_id`    bigint(16) NOT NULL DEFAULT '0' COMMENT '上级评论ID（0为顶级评论）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '新闻评论';

DROP TABLE IF EXISTS `news_user`;
CREATE TABLE `news_user` (
    `id`           bigint(16) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `gmt_create`   datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `mobile`       varchar(16) DEFAULT NULL COMMENT '手机号',
    `password`     varchar(64) DEFAULT NULL COMMENT '密码（SHA1加密）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '新闻用户';

SET FOREIGN_KEY_CHECKS = 1;
```

### 架构图设计

对于现代微服务架构来说，我们在搭建项目之前最好先设计架构图，因为微服务工程较多，关系比较复杂，有了架构图，更有利于我们进行架构设计，下面请看本实例的架构图：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2afpb9nj30yc0omjvf.jpg" style="zoom:50%;" />

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggh2ag6727j31280skth7.jpg" style="zoom:50%;" />

### 框架搭建

根据架构图，我们就可以开始搭建框架，首先要进行技术选型，也就是需要集成什么技术，本实例，我们将能够看到注册中心、配置中心、服务网关、Redis、MySQL、API 鉴权等技术，下面请看具体代码。

我们知道，微服务架构其实是由多个工程组成的，根据架构图，我们就可以先把所有工程创建好

其中，common 不是一个项目工程，而是公共类库，所有项目都依赖它，我们可以把公共代码放在 common 下，比如字符串的处理、日期处理、Redis 处理、JSON 处理等。

client 包括客户端工程，config 为配置中心，gateway 为服务网关，register 为注册中心。

本课我们先来搭建注册中心、配置中心和服务网关。

1. #### 注册中心

   首先创建启动类：

   ```java
   @SpringCloudApplication
   @EnableEurekaServer
   public class Application {
     public static void main(String[] args) {
       SpringApplication.run(Application.class,args);
     }
   }
   ```

   然后创建 YAML 配置文件：

   ```yaml
   server:
     port: 8888
   spring:
     application:
       name: eurekaserver
     profiles:
       active: dev
     cloud:
       inetutils:
         preferred-networks: 127.0.0.1
       client:
         ip-address: 127.0.0.1
   eureka:
     server:
       peer-node-read-timeout-ms: 3000
       enable-self-preservation: true
     instance:
       prefer-ip-address: false
       instance-id: ${spring.cloud.client.ip-address}:${server.port}
     client:
       registerWithEureka: true
       fetchRegistry: false
       healthcheck:
         enabled: true
       serviceUrl:
         defaultZone: http://127.0.0.1:8888/eureka/
   ```

2. #### 配置中心

   创建启动类：

   ```java
   @SpringCloudApplication
   @EnableConfigServer
   public class Application {
     public static void main(String[] args) {
       SpringApplication.run(Application.class,args);
     }
   }
   ```

   创建 YAML 配置文件：

   ```yaml
   server:
     port: 8101
   spring:
     application:
       name: config
     profiles:
       active: dev
     cloud:
       config:
         server:
           git:
             uri: https://github.com/springcloudlynn/springcloudinactivity # 配置 git 仓库地址
             searchPaths: repo # 配置仓库路径
             username: springcloudlynn # 访问 git 仓库的用户名
             password: ly123456 # 访问 git 仓库的用户密码
         label: master # 配置仓库的分支
     rabbitmq:
       host: 127.0.0.1
       port: 5672
       username: guest
       password: guest
       virtualHost: /
       publisherConfirms: true
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8888/eureka/
   management:
     endpoints:
       web:
         exposure:
           include: refresh,health,info,bus-refresh
   ```

3. #### 服务网关

   首先是启动类：

   ```java
   @SpringCloudApplication
   public class Application {
     public static void main(String[] args) {
       SpringApplication.run(Application.class,args);
     }
   }
   ```
   
   服务网关的配置可以通过配置中心拉下来，下面是配置文件代码，此时配置文件名字为 bootstrap.yml：
   
   ```yaml
   spring:
    application:
       name: gateway
    profiles:
       active: dev
     cloud:
       config:
         name: gateway,key
         label: master
         discovery:
           enabled: true
           serviceId: config
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8888/eureka/
   ```

本课的基础框架就搭建到这里，后面将继续搭建基础框架。

