# Hystrix 容错监控机制

title: 12-Hystrix 容错监控机制
date: 2020-07-10 15:09:26
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

在基于微服务的分布式系统中，每个业务模块都可以拆分为一个独立自治的服务，多个请求来协同完成某个需求，在一个具体的业务场景中某个请求可能需要同时调用多个服务来完成。这就存在一个问题，**多个微服务所对应的配置项也会非常多**，一旦某个微服务进行了修改，则其他服务也需要作出调整，直接在**每个微服务中修改对应的配置项是非常麻烦的**，**改完之后还需要重新部署项目**。

微服务是分布式的，但是我们希望可以**对所有微服务的配置文件进行集中统一管理**，便于部署和维护，Spring Cloud 提供了对应的解决方案，即 Spring Cloud Config，通过服务端可以为多个客户端提供配置服务。

Spring Cloud Config 可以将配置文件**存放在本地**，也可以**存放在远程 Git 仓库**中。拿远程 Git 仓库来说，具体的操作思路是**将所有的外部配置文件集中放置在 Git 仓库中，然后创建 Config Server，通过它来管理所有的配置文件，需要更改某个微服务的配置信息时，只需要在本地进行修改，然后推送到远程 Git 仓库即可，所有的微服务实例都可以通过 Config Server 来读取对应的配置信息**。

### 搭建本地 Config Server
> 我们可以将微服务的相关配置文件存储在本地文件中，然后让微服务来读取本地配置文件，具体操作如下

#### 创建本地 Config Server

1. 在父工程下创建名为 nativeconfigserver 的 Module

2. 在 pom.xml 中添加 Spring Cloud Config 依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-config-server</artifactId>
   </dependency>
   ```

3. 在配置文件 application.yml 添加 Config Server 相关配置

   ```yaml
   server:
     port: 8762 # 当前 Config Server 服务端口
   spring:
     application:
       name: nativeconfigserver # 当前服务注册在 Eureka Server 上的名称
     profiles:
       active: native # 配置文件获取方式
     cloud:
       config:
         server:
           native:
             search-locations: classpath:/shared # 本地配置文件的存放路径
   ```

4. 在 resources 路径下新建 shared 文件夹，并在此目录下创建本地配置文件 configclient-dev.yml，定义 port 和 foo 信息

   ```yaml
   server:
     port: 8070
   foo: foo version 1
   ```

5. 在启动类 NativeConfigServerApplication 添加注解

   ```java
   @EnableConfigServer // 声明配置中心
   ```

本地配置中心已经创建完成，接下来创建客户端来读取本地配置中心的配置文件

#### 创建Config Client客户端

1. 在父工程下创建名为 nativeconfigclient 的 Module

2. 在 pom.xml 中添加 Spring Cloud Config 依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-config</artifactId>
   </dependency>
   ```

3. 在 resources 路径下创建 bootstrap.yml，配置读取本地配置中心的相关信息

   ```yaml
   spring:
     application:
       name: nativeconfigclient # 当前服务注册在 Eureka Server 上的名称
     profiles:
       active: dev # 配置文件名，这里需要与当前微服务在 Eureka Server 注册的名称结合起来使用，两个值用 - 连接，比如当前微服务的名称是 configclient，profiles.active 的值是 dev，那么就会在本地 Config Server 中查找名为 configclient-dev 的配置文件
     cloud:
       config:
         uri: http://localhost:8762 # 本地 Config Server 的访问路径
         fail-fast: true # 设置客户端优先判断 config server 获取是否正常，并快速响应失败内容
   ```

4. 创建 NativeConfigHandler，定义相关业务方法

   ```java
   @RestController
   @RequestMapping("/native")
   public class NativeConfigHandler {
     @Value("${server.port}")
     private String port;
     @Value("${foo}")
     private String foo;
   
     @GetMapping("/index")
     public String index() {
       return this.port + "-" + this.foo;
     }
   }
   ```

5. 依次启动注册中心、 NativeConfigServer、ConfigClient。访问 index() 方法并打印：`8070-foo version 1`，说明读取本地配置成功

   ```shell
   curl -X GET http://localhost:8070/native/index
   ```

### 总结
本节课我们讲解了使用 Spring Cloud Config 来实现本地配置的具体操作，Spring Cloud Config 包括服务端（**Config Server**）和客户端（**Config Client**），**提供了分布式系统外部化配置的功能**，下节课我们来学习远程配置中心的搭建方式。