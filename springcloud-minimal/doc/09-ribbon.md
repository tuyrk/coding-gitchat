# Ribbon 负载均衡

在前面的课程中我们已经通过 RestTemplate 实现了服务消费者对服务提供者的调用，这只是实现了最基本的需求，如果在某个具体的业务场景下，对于某服务的调用需求激增，这时候我们就需要为该服务实现负载均衡以满足高并发访问，在一个大型的分布式应用系统中，负载均衡(Load Balancing)是必备的。

### 什么是 Ribbon？
Spring Cloud 提供了实现负载均衡的解决方案：Spring Cloud Ribbon，Ribbon 是 Netflix 发布的负载均衡器，而 Spring Cloud Ribbon 则是基于 Netflix Ribbon 实现的，是一个用于对 HTTP 请求进行控制的负载均衡客户端。

Spring Cloud Ribbon 官网地址：http://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html

Ribbon 的使用同样需要结合 Eureka Server，即**需要将 Ribbon 在 Eureka Server 进行注册**，注册完成之后，就可以通过 Ribbon 结合某种负载均衡算法，如随机、轮询、加权随机、加权轮询等帮助服务消费者去调用接口。除了 Ribbon 默认提供的这些负载均衡算法外，开发者也可以根据具体需求来设计自定义的 Ribbon 负载均衡算法。实际开发中，Spring Cloud Ribbon 需要结合 Spring Cloud Eureka 来使用，Eureka Server 提供所有可调用的服务提供者列表，Ribbon 基于特定的负载均衡算法从这些服务提供者中挑选出要调用的实例，如下图所示。

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1ggl6w252qqj30wm0oujv5.jpg" alt="1" style="zoom:40%;" />

### Ribbon 常用的==负载均衡策略==有以下几种：

- 随机：访问服务时，随机从注册中心的服务列表中选择一个。
- 轮询：当同时启动两个服务提供者时，客户端请求会由这两个服务提供者交替处理。
- 加权轮询：对服务列表中的所有**微服务响应时间做加权处理**，并以轮询的方式来访问这些服务。
- 最大可用：从服务列表中选择**并发访问量最小的微服务**。

### 使用 Ribbon 实现负载均衡

1. 在父工程下创建名为 ribbon 的 Module

2. 在 pom.xml 中添加 Eureka Client 依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

3. 在配置文件 application.yml 添加 Ribbon 相关配置

   ```yaml
   server:
     port: 8040 was # 当前 Ribbon 服务端口
   spring:
     application:
       name: ribbon # 当前服务注册在 Eureka Server 上的名称
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
     instance:
       prefer-ip-address: true # 是否将当前服务的 IP 注册到 Eureka Server
   ```

4. 通过 @Bean 注解注入 RestTemplate 实例，==@LoadBalanced 注解提供负载均衡==。

   ```java
   @Bean
   @LoadBalanced // 声明一个基于 Ribbon 的负载均衡
   public RestTemplate restTemplate(){
     return new RestTemplate();
   }
   ```

5. 使用 Ribbon 调用 Provider 服务

   - 创建接口对应的实体类 Student

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

   - 创建 RibbonHandler，通过 RestTemplate 调用 Provider 相关接口

     **RestTemplate 访问的 URL 不需要指定 IP 和 端口，直接访问 Provider 在 Eureka Server 注册的 application name 即可**。
     比如：http://localhost:8010/ 可替换为 http://provider/
     
     ```java
     @RestController
     @RequestMapping("/ribbon")
     public class RibbonHandler {
       @Autowired
       private RestTemplate restTemplate;
     
       @GetMapping("/findAll")
       public Collection<Student> findAll() {
         return restTemplate.getForObject("http://provider/student/findAll", Collection.class);
       }
     }
     ```

6. 依次启动注册中心、Provider、Ribbon。打开浏览器，访问 http://localhost:8761

   可以看到 Provider 和 Ribbon 已经在注册中心完成注册，接下来用 Postman 工具测试 Ribbon 相关接口

   ```shell
   curl -X GET http://localhost:8040/ribbon/index
   curl -X GET http://localhost:8040/ribbon/findAll
   curl -X GET http://localhost:8040/ribbon/findById/1
   curl -X POST http://localhost:8040/ribbon/save -d '{"id":4,"name":"tuyrk","gender":"男"}' -H "Content-Type: application/json"
   curl -X PUT http://localhost:8040/ribbon/update -d '{"id":4,"name":"tyk","gender":"男"}' -H "Content-Type: application/json"
   curl -X DELETE http://localhost:8040/ribbon/deleteById/1
   ```

7. 测试 Ribbon 的负载均衡，在 RibbonHandler 中添加如下代码。

   ```java
   @GetMapping("/index")
   public String index(){
     return restTemplate.getForObject("http://provider/student/index",String.class);
   }
   ```

8. 分别启动注册中心、端口为 8010 的 Provider、端口为 8011 的 Provider、Ribbon。打开浏览器，访问 http://localhost:8761

   可以看到两个 Provider 和 Ribbon 已经在注册中心完成注册

9. 访问请求服务方法，端口为 8010 和 8011 的微服务交替被访问，实现了负载均衡

   ```shell
   curl -X GET http://localhost:8040/ribbon/index
   ```

### 总结
本节课我们讲解了使用 Ribbon 来实现服务调用负载均衡的具体操作，Ribbon 是 Netflix 发布的负载均衡器，Ribbon 的功能是结合某种负载均衡算法，如轮询、随机、加权轮询、加权随机等帮助服务消费者去调用接口，同时也可以自定义 Ribbon 的负载均衡算法