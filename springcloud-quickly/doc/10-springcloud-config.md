# 第09课：配置中心

title: 10-第09课：配置中心
date: 2020-07-01 18:03:47
categories: [GitChat,SpringCloud快速入门]
tags: [springcloud]

---

通过前面章节，我们已经学习了 SpringCloud 的很多组件，每个组件都创建了一个工程，而每个工程都会有一个配置文件，并且有些配置是一样的。例如：在实际项目中，我们创建了用户和订单两个服务，这两个服务是同一个数据库，那么我们在这两个服务的配置文件都会配置相同的数据源，一旦我们的数据库地址发生改变（只是一种情况），用户和订单两个服务的配置文件都需要改，这还是只是两个服务，在一个大型系统（比如淘宝），将会有成千上万个服务，按照这种方式代价无疑是巨大的。

不过无需担心，正所谓上有政策，下有对策，既然有这个问题，就一定会有解决方案，那就是创建一个配置中心，专门用于管理系统的所有配置，也就是我们将所有配置文件放到统一的地方进行管理。

我们知道，SpringCloud 就是为了简化开发而生的，因此 SpringCloud 为我们集成了配置中心——Spring Cloud Config 组件。

### Spring Cloud Config 简介

Spring Cloud Config 是一个高可用的分布式配置中心，它支持将配置存放到内存（本地），也支持将其放到 Git 仓库进行统一管理（本课主要探讨和 Git 的融合）。

### 创建配置中心

创建配置中心一般分为以下几个步骤：

1. 创建 Git 仓库。

   本文为了演示实例，已经创建好了用于存放配置文件的 Git 仓库，[点击这里](https://github.com/lynnlovemin/SpringCloudLesson.git)访问。

2. 创建配置中心。

   在原有工程创建一个 moudle，命名为 config，在 pom.xml 加入配置中心的依赖：

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-config-server</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   ```

   启动类 Application.java 添加注解

   ```java
   @EnableConfigServer // 开启配置中心
   ```
   
   创建 application.yml 并增加如下内容：
   
   ```yaml
   server:
     port: 8888
spring:
     application:
    name: config
     profiles:
       active: dev
     cloud:
       config:
         server:
           git:
             uri: https://github.com/tuyrk/SpringCloudLesson.git #配置git仓库地址
             searchPaths: 第09课/config #配置仓库路径
             username: ****** #访问git仓库的用户名
             password: ****** #访问git仓库的用户密码
         label: master #配置仓库的分支
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8761/eureka/
   ```
   
   首先分别启动注册中心 eurekaserver 和配置中心 config，浏览器访问：http://localhost:8761，我们可以看到如下界面：
   
   ![](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggbmosajqzj31m40u0jz9.jpg)
   
   通过上述过程，配置服务中心已经创建完成，控制台输入以下命令并出现：
   
   ```shell
   > curl http://localhost:8888/config/dev
   {"name":"config","profiles":["dev"],"label":null,"version":"d4042a9a6e1b5904ae4902d85c3112a96d694db6","state":null,"propertySources":[]}
   ```
```
   
3. 修改各个服务配置。

   我们创建配置中心的目的就是为了方便其他服务进行统一的配置管理，因此，还需要修改各个服务。

   以服务提供者 eurekaclient 为例，按照以下步骤进行操作。

   在 pom.xml 加入配置中心依赖：

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-config</artifactId>
   </dependency>
```

   在 resources 下新建 bootstrap.yml 并删除 application.yml：

   ```yaml
   spring:
     cloud:
       config:
         name: eurekaclient
         label: master
         discovery:
           enabled: true
           serviceId: config
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/
   ```

   在配置中心配置的 Git 仓库相应路径下创建配置文件 [eurekaclient.yml](https://github.com/tuyrk/SpringCloudLesson/blob/master/第09课/ config/eurekaclient.yml):

   ```yaml
   server:
     port: 8763
   spring:
     application:
       name: eurekaclient
   ```

   我们依次启动注册中心、配置中心和服务提供者 eurekaclient，可以看到 eurekaclient 的监听端口为 8763，然后修改 eurekaclient.yml 的 server.port 为8764，重新启动 eurekaclient，可以看到其监听端口为 8764，说明 eurekaclient 成功从 Git 上拉取了配置。

### 配置自动刷新

我们注意到，每次修改配置都需要重新启动服务，配置才会生效，这种做法也比较麻烦，因此我们需要一个机制，每次**修改了配置文件，各个服务配置自动生效**，Spring Cloud 给我们提供了解决方案。

#### 手动刷新配置

1. 在 eurekaclient 工程的 pom.xml 添加依赖：

   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

2. 修改远程 Git 仓库的配置文件 eurekaclient.yml，增加以下内容：

   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: refresh,health,info
   ```

   Spring Boot 2.0 以后，actuator 默认只开启 health 和 info 端点，要使用 refresh 端点需要通过 management 指定。

3. 在 HelloController 类加入 `@RefeshScope` 依赖


以上步骤就集成了手动刷新配置。下面开始进行测试。

1. 依次启动注册中心，配置中心，客户端；

2. 控制台输入以下命令并出现：

   ```shell
   > curl http://localhost:8763/index
   Hello World!,端口：8763
   ```

3. 修改 Git 仓库远程配置文件 eurekaclient.yml 的端口为8764；

4. 重复步骤2，我们发现端口未发生改变；

5. refresh 端点请求配置中心刷新配置：

   ```shell
   > curl -X POST http://localhost:8763/actuator/refresh
   # 重新启动日志信息
   ```
   
6. 重复步骤2，我们发现端口已发生改变，说明刷新成功！

   ```
   Hello World!,端口：8764
   ```

#### 自动刷新配置

前面我们讲了通过 `/refresh` 端点手动刷新配置，如果每个微服务的配置都需要我们手动刷新，代价无疑是巨大的。不仅如此，随着系统的不断扩张，维护也越来越麻烦。因此，我们有必要实现自动刷新配置。

##### **自动刷新配置原理**

1. 利用 Git 仓库的 WebHook，可以设置当有内容 Push 上去后，则通过 HTTP 的 POST 远程请求指定地址。
2. 利用消息队列如 RabbitMQ、Kafka 等自动通知到每个微服务（本文以 RabbitMQ 为例讲解）。

##### **实现步骤**

> 下面我们就来实现自动刷新配置。

1. 安装 RabbitMQ（安装步骤省略，请自行百度）。

2. 在 eurekaclient 加入如下依赖：

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-bus-amqp</artifactId>
   </dependency>
   ```

3. 在 config的application.yml 添加以下内容：

   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: refresh,health,info,bus-refresh
   spring:
     rabbitmq:
       host: 127.0.0.1
       port: 5672
       username: guest
       password: guest
       virtualHost: /
       publisherConfirms: true
   ```

4. 启动注册中心、配置中心和客户端；

5. 发送 POST 请求，可以看到配置已被刷新。

   ```shell
   > curl -X POST http://localhost:8763/actuator/bus-refresh
   ```

6. 利用 Git 的 WebHook，实现自动刷新，如图：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggbmoso062j30z70u0tdq.jpg" alt="enter image description here" style="zoom:50%;" />

设置好刷新 URL 后，点击提交。以后每次有新的内容被提交后，会自动请求该 URL 实现配置的自动刷新。