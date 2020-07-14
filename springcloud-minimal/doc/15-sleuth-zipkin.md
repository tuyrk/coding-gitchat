# Zipkin 服务跟踪

title: 15-Zipkin 服务跟踪
date: 2020-07-13 11:34:05
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

本节课我们来学习服务跟踪。首先来思考一个问题，为什么要有服务跟踪，我们知道一个分布式系统中往往会部署很多个微服务，这些**服务彼此之间会相互调用，整个过程就会较为复杂**，我们**在进行问题排查或者优化的时候工作量就会比较大**。**如果能准确跟踪每一个网络请求的整个运行流程，获取它在每个微服务上的访问情况、是否有延迟、耗费时间等，这样的话我们分析系统性能，排查解决问题就会容易很多**，我们使用 Zipkin 组件来实现服务跟踪。

### 什么是 Zipkin
**Zipkin 是一个可以采集并且跟踪分布式系统中请求数据的组件**，可以为开发者**采集某个请求在多个微服务之间的追踪数据，并以可视化的形式呈现**出来，让开发者可以更加直观地了解到请求在各个微服务中所耗费的时间等信息。

ZipKin 组件包括两部分：Zipkin Server 和 Zipkin Client，服务端用来采集微服务之间的追踪数据，再通过客户端完成数据的生成和展示，Spring Cloud 为服务跟踪提供了解决方案，Spring Cloud Sleuth 集成了 Zipkin 组件。

接下来我们通过实际代码来完成服务跟踪的实现，首先来实现 Zipkin Server。

### 实现 Zipkin Server

1. 在父工程下创建名为 zipkin 的 Module

2. 在 pom.xml 中添加 Zipkin Server 依赖。

   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   <dependency>
     <groupId>io.zipkin.java</groupId>
     <artifactId>zipkin-server</artifactId>
     <version>2.12.9</version>
   </dependency>
   <dependency>
     <groupId>io.zipkin.java</groupId>
     <artifactId>zipkin-autoconfigure-ui</artifactId>
     <version>2.12.9</version>
   </dependency>
   ```

3. 在 application.yml 添加 Zipkin 相关配置

   ```yaml
   server:
     port: 9090 # 当前 Zipkin Server 服务端口
   ```

4. 在启动类 ZipkinApplication 添加注解

   ```java
   @EnableZipkinServer // 声明启动 Zipkin Server
   ```

Zipkin Server 搭建成功，接下来创建 Zipkin Client

### 实现 Zipkin Client

1. 在父工程下创建名为 zipkinclient 的 Module

2. 在 pom.xml 中添加 Zipkin 依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-zipkin</artifactId>
   </dependency>
   ```

3. 在 application.yml 添加 Zipkin 相关配置

   ```yaml
   server:
     port: 8090 # 当前 Zipkin Client 服务端口
   spring:
     application:
       name: zipkinclient # 当前服务注册在 Eureka Server 上的名称
     sleuth:
       web:
         client:
           enabled: true # 设置是否开启 Sleuth
       sampler:
         probability: 1.0 # 设置采样比例，默认是 0.1
     zipkin:
       base-url: http://localhost:9090/ # Zipkin Server 地址
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
   ```

4. 创建 ZipkinHandler，定义相关业务方法

   ```java
   @RestController
   @RequestMapping("/zipkin")
   public class ZipkinHandler {
     @Value("${server.port}")
     private String port;
   
     @GetMapping("/index")
     public String index(){
       return "当前端口："+this.port;
     }
   }
   ```

5. 依次启动注册中心、Zipkin、ZipkinClient。打开浏览器访问 http://localhost:9090/zipkin/，可看到 Zipkin 首页。

6. 点击 Find Traces 按钮可看到监控数据情况，当前没有监控到任何数据。

7. 访问请求后再次刷新 http://localhost:9090/zipkin/，可看到监控数据，点击可查看详情。

   ```shell
   curl -X GET http://localhost:8090/zipkin/index
   ```

### 总结
本节课我们讲解了使用 Zipkin 来实现服务链路追踪的具体操作，通过服务跟踪，我们可以追踪到每个网络请求，了解它整个运行流程，经过了哪些微服务、是否有延迟、耗费时间等，在此基础上我们能够更好的分析系统性能，解决系统问题。