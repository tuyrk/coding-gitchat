# Spring Cloud Feign 声明式接口调用

title: 10-Spring Cloud Feign 声明式接口调用
date: 2020-07-10 10:20:12
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

上节课我们学习了使用 **Ribbon + RestTemplate** 实现服务调用的负载均衡，在实际开发中，还有另外一种更加便捷的方式来实现同样的功能，这就是 **Feign**，本节课中我们就来学习使用 Feign 实现服务消费的负载均衡。

### 什么是 Feign
与 Ribbon 一样，Feign 也是由 Netflix 提供的，**Feign 是一个提供模版的声明式 Web Service 客户端**， 使用 Feign 可以简化 Web Service 客户端的编写，开发者可以通过简单的接口和注解来调用 HTTP API，使得开发更加快捷、方便。Spring Cloud 也提供了 Feign 的集成组件：Spring Cloud Feign，它**整合了 Ribbon 和 Hystrix**，具有可插拔、基于注解、负载均衡、服务熔断等一系列便捷功能。

在 Spring Cloud 中使用 Feign 非常简单，我们说过 Feign 是一个声明式的 Web Service 客户端，所以只需要创建一个接口，同时在接口上添加相关注解即可完成服务提供方的接口绑定，相比较于 Ribbon + RestTemplate 的方式，Feign 大大简化了代码的开发，Feign 支持多种注解，包括 Feign 注解、JAX-RS 注解、Spring MVC 注解等。Spring Cloud 对 Feign 进行了优化，整合了 Ribbon 和 Eureka，从而让 Feign 的使用更加方便。

我们说过 Feign 是一种比 Ribbon 更加方便好用的 Web 服务客户端，那么二者有什么具体区别呢？Feign 好用在哪里？

### Ribbon 与 Feign 的区别
关于 Ribbon 和 Feign 的区别可以简单地理解为 **Ribbon 是个通用的 HTTP 客户端工具**，而 **Feign 则是基于 Ribbon 来实现的**，同时它更加灵活，使用起来也更加简单。上节课中我们通过 Ribbon + RestTemplate 实现了服务调用的负载均衡，相比较于这种方式，使用 Feign 可以直接通过声明式接口的形式来调用服务，非常方便，比 Ribbon 使用起来要更加简便，只需要创建接口并添加相关注解配置，即可实现服务消费的负载均衡。

### Feign 的特点

- Feign 是一个声明式 Web Service 客户端。
- 支持 Feign 注解、JAX-RS 注解、Spring MVC 注解。
- Feign 基于 Ribbon 实现，使用起来更加简单。
- Feign 集成了 Hystrix，具备服务熔断功能。

### 使用 Feign 实现服务调用

1. 在父工程下创建名为 feign 的 Module

2. 在 pom.xml 中添加 Eureka Client 和 Feign 依赖

   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-openfeign</artifactId>
   </dependency>
   ```

3. 在配置文件 application.yml 添加 Feign 相关配置

   ```yaml
   server:
     port: 8050 # 当前 Feign 服务端口
   spring:
     application:
       name: feign # 当前服务注册在 Eureka Server 上的名称
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
     instance:
       prefer-ip-address: true # 是否将当前服务的 IP 注册到 Eureka Server
   ```

4. 在启动类 FeignApplication 添加`@EnableFeignClients`注解，用于声明启用 Feign

5. 通过接口的方式调用 Provider 服务

   - 创建对应的实体类 Student

     ```java
     @Data
     @AllArgsConstructor
     @NoArgsConstructor
     public class Student {
       private long id;
       private String name;
       private char gender;
     }
     ```

   - 创建 FeignProviderClient 接口

     ```java
     @FeignClient(value = "provider") // 指定 Feign 要调用的微服务，直接指定服务提供者在注册中心的 application name 即可
     public interface FeignProviderClient {
       @GetMapping("/student/index")
       public String index();
       @GetMapping("/student/findAll")
       public Collection<Student> findAll();
       @GetMapping("/student/findById/{id}")
       public Student findById(@PathVariable("id") long id);
       @PostMapping("/student/save")
       public void save(@RequestBody Student student);
       @PutMapping("/student/update")
       public void update(@RequestBody Student student);
       @DeleteMapping("/student/deleteById/{id}")
       public void deleteById(@PathVariable("id") long id);
     }
     ```

   - 创建 FeignHandler，通过 @Autowired 注入 FeignProviderClient 实例，完成相关业务

     ```java
     @RequestMapping("/feign")
     @RestController
     public class FeignHandler {
       @Autowired
       private FeignProviderClient feignProviderClient;
     
       @GetMapping("/index")
       public String index(){
         return feignProviderClient.index();
       }
       @GetMapping("/findAll")
       public Collection<Student> findAll(){
         return feignProviderClient.findAll();
       }
     }
     ```

7. 依次启动注册中心、端口为 8010 的 Provider、端口为 8011 的 Provider、Feign。打开浏览器访问 http://localhost:8761

   可以看到两个 Provider 和 Feign 已经在注册中心完成注册

   **注**：需添加web依赖，否则出现异常

8. 使用 Postman 工具测试 Feign 相关接口

   ```shell
   curl -X GET http://localhost:8050/feign/index
   curl -X GET http://localhost:8050/feign/findAll
   curl -X GET http://localhost:8050/feign/findById/1
   curl -X POST http://localhost:8050/feign/save -d '{"id":4,"name":"tuyrk","gender":"男"}' -H "Content-Type: application/json"
   curl -X PUT http://localhost:8050/feign/update -d '{"id":4,"name":"tyk","gender":"男"}' -H "Content-Type: application/json"
   curl -X DELETE http://localhost:8050/feign/deleteById/1
   ```

13. Feign 也提供了负载均衡功能。访问请求服务方法，端口为 8010 和 8011 的微服务交替被访问

    ```shell
    curl -X GET http://localhost:8050/feign/index
    ```

14. Feign 同时提供了容错功能。如果服务提供者 Provider 出现故障无法访问，访问 Feign 会直接报错：Internal Server Error

    显然这种直接返回错误状态码的交互方式很不友好，可以通过容错机制给用户相应的提示信息，而非错误状态码，使得交互方式更加友好。使用容错机制非常简单：

    - 在 application.yml 中添加配置

      ```yaml
      feign:
        hystrix:
          enabled: true # 是否开启熔断器
      ```

    - 创建 FeignProviderClient 接口的实现类 FeignError ，定义容错处理逻辑，通过 @Component 将 FeignError 实例注入 IoC 容器
    
      ```java
      @Component
      public class FeignError implements FeignProviderClient {
        @Override
        public String index() { return "服务器维护中...."; }
        @Override
        public Collection<Student> findAll() { return null; }
      }
      ```
    
    - 在 FeignProviderClient 定义处通过 @FeignClient 的 fallback 属性设置映射
    
      ```java
      @FeignClient(value = "provider", fallback = FeignError.class)
      public interface FeignProviderClient { }
      ```
    
    - 启动注册中心和 Feign，此时没有服务提供者 Provider 被注册，直接访问 Feign 接口提示：服务器维护中....
    
      ```shell
      curl -X GET http://localhost:8050/feign/index
      ```

### 总结
本节课我们讲解了使用 Feign 来实现服务消费负载均衡的具体操作，Feign 是一个提供模版的声明式 Web Service 客户端，可以帮助开发者更加方便地调用服务接口，并实现负载均衡，Feign 和 Ribbon + RestTemplate 都可以完成服务消费的负载均衡，**实际开发中推荐使用 Feign**。