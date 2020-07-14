# 用服务网关统一 URL，开发更简洁

title: 08-用服务网关统一 URL，开发更简洁
date: 2020-07-09 19:17:55
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

### 微服务网关使用场景

在分布式项目架构中，我们会将服务进行拆分，不同的微服务负责各自的业务功能，实现软件架构层面的解耦合。但是如果拆分之后的微服务数量太多，是不利于系统开发的，因为每个服务都有不同的网络地址，客户端多次请求不同的微服务需要调用不同的 URL，如果同时去维护多个不同的 URL 无疑会增加开发的成本。

如下图所示，一个外卖订餐系统，需要调用多个微服务接口才能完成一次订餐的业务流程，如果能有一种解决方案可以统一管理不同的微服务 URL，肯定会增强系统的维护性，提高开发效率。

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggkxsdxo2oj31o00sk42q.jpg" alt="1" style="zoom: 30%;" />

这个解决方案就是 API 网关，**API 网关对所有的 API 请求进行管理维护，相当于为系统开放出一个统一的接口，所有的外部请求只需要访问这个统一入口即可，系统内部再通过 API 网关去映射不同的微服务**。对于开发者而言就不需要关注具体的微服务 URL 了，直接访问 API 网关接口即可，API 网关的结构如下图所示。

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggkxsff379j31eo0u07a0.jpg" alt="2" style="zoom:30%;" />

### 使用 Zuul 实现微服务网关

> Spring Cloud 集成了 Zuul

1. 在父工程下创建名为 gateway 的 Module

2. 在 pom.xml 中添加 Zuul 和 Eureka Client 依赖，Zuul 也作为一个 Eureka Client 在注册中心完成注册。

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

3. 在 resources 路径下创建配置文件 application.yml，添加网关相关配置

   ```yaml
   server:
     port: 8030 # 当前 gateway 服务端口
   spring:
     application:
       name: gateway # 当前服务注册在 Eureka Server 上的名称
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
   zuul:
     routes: # 自定义微服务的访问路径
       provider: /p/** # provider 微服务就会被映射到 gateway 的 /p/** 路径
   ```

4. 在启动类 GateWayApplication 添加`@EnableZuulProxy`、`@EnableAutoConfiguration`注解

   - @EnableZuulProxy 包含 @EnableZuulServer 的功能，而且还加入了 @EnableCircuitBreaker 和 @EnableDiscoveryClient
   - @EnableAutoConfiguration 将所有符合条件的 @Configuration 配置都加载到当前 Spring Boot 创建并使用的 IoC 容器。

5. 依次启动注册中心、服务提供者 provider，运行 GateWayApplication。打开浏览器，访问 http://localhost:8761

   可以看到服务提供者 provider 和网关 gateway 已经在 Eureka Server 完成注册

6. 通过 http://localhost:8030/p/student/findAll 访问 provider 提供的相关服务

   ```shell
   curl -X GET http://localhost:8030/p/student/findAll
   ```

7. 同时 Zuul 自带了负载均衡功能。在服务提供者 provider 添加返回当前服务端口的方法：

   ```java
   @Value("${server.port}")
   private String port;
   
   @GetMapping("/index")
   public String index() {
     return "当前端口：" + this.port;
   }
   ```

8. 复制idea启动实例，设置VM options参数`-Dserver.port=8011`，再启动实例。最后重新启动 gateway，访问 http://localhost:8761

   可以看到当前注册中心有两个 provider 服务

9. 通过 gateway 来访问请求服务方法，端口为 8010 和 8011 的微服务交替被访问，实现了负载均衡

   ```shell
   curl -X GET http://localhost:8030/p/student/index
   ```

### 总结

本节课我们讲解了使用 **Zuul 组件实现服务网关**的具体操作，Zuul 需要结合 Eureka Client 在注册中心完成注册，Zuul 是一个在云平台上提供动态路由，监控，弹性，安全等边缘服务的框架，相当于客户端和 Netflix 流应用 Web 网站后端所有请求的中间层，可以简化代码的开发。