# 服务消费者

在前面的课程中，我们通过 Eureka Client 组件创建了一个服务提供者 provider，并且在注册中心完成注册，接下来其他微服务就可以访问 provider 相关服务了。

本节课我们就来实现一个服务消费者 consumer，调用 provider 相关接口，实现思路是先通过 Spring Boot 搭建一个微服务应用，再通过 Eureka Client 将其注册到 Eureka Server。此时的 provider 和 consumer 从代码的角度看并没有区别，都是 Eureka 客户端，我们人为地从业务角度对它们进行区分，provider 提供服务，consumer 调用服务，具体的实现需要结合 RestTemplate 来完成，即在服务消费者 consumer 中通过 RestTemplate 来调用服务提供者 provider 的相关接口。

![](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggkpcvo14qj316w0oan23.jpg)

### 代码实现服务消费者Eureka Client

1. 在父工程下创建名为 consumer 的 Module，实现 Eureka Client。

2. 在 pom.xml 中添加 Eureka Client 依赖。

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

3. 在 resources 路径下创建配置文件 application.yml，添加 Eureka Client 相关配置，此时的 Eureka Client 为服务消费者 consumer

   ```yaml
   server:
     port: 8020 # 当前 Eureka Client 服务端口
   spring:
     application:
       name: consumer # 当前服务注册在 Eureka Server 上的名称
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
     instance:
       prefer-ip-address: true # 是否将当前服务的 IP 注册到 Eureka Server
   ```

4. 现在让 consumer 调用 provider 提供的服务，首先创建实体类 Student。

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

5. 修改 ConsumerApplication 代码，创建 RestTemplate 实例并通过 @Bean 注解注入到 IoC 容器中

   ```java
   @Bean
   public RestTemplate restTemplate() {
     return new RestTemplate();
   }
   ```

6. 创建 StudentHandler，注入 RestTemplate 实例，业务方法中通过 RestTemplate 来调用 provider 的相关服务。

   ```java
   @RequestMapping("/consumer")
   @RestController
   public class StudentHandler {
     @Autowired
     private RestTemplate restTemplate;
   
     @GetMapping("/findAll")
     public Collection<Student> findAll(){
       return restTemplate.getForObject("http://localhost:8010/student/findAll",Collection.class);
     }
     @GetMapping("/findById/{id}")
     public Student findById(@PathVariable("id") long id){
       return restTemplate.getForObject("http://localhost:8010/student/findById/{id}",Student.class,id);
     }
     @PostMapping("/save")
     public void save(@RequestBody Student student){
       restTemplate.postForObject("http://localhost:8010/student/save",student,Student.class);
     }
     @PutMapping("/update")
     public void update(@RequestBody Student student){
       restTemplate.put("http://localhost:8010/student/update",student);
     }
     @DeleteMapping("/deleteById/{id}")
     public void deleteById(@PathVariable("id") long id){
       restTemplate.delete("http://localhost:8010/student/deleteById/{id}",id);
     }
   }
   ```

7. 依次启动注册中心、服务提供者 provider，并运行 ConsumerApplication，打开浏览器访问 http://localhost:8761

   可以看到服务提供者 provider 和服务消费者 consumer 已经在 Eureka Server 完成注册

8. 访问 consumer 的相关服务了，通过 Postman 工具测试 consumer 接口

   ```shell
   curl -X GET http://localhost:8020/consumer/findAll
   curl -X GET http://localhost:8020/consumer/findById/1
   curl -X POST http://localhost:8020/consumer/save -d '{"id":4,"name":"tuyrk","gender":"男"}' -H "Content-Type: application/json"
   curl -X PUT http://localhost:8020/consumer/update -d '{"id":4,"name":"tyk","gender":"男"}' -H "Content-Type: application/json"
   curl -X DELETE http://localhost:8020/consumer/deleteById/1
   ```

### 总结
本节课我们讲解了使用 Eureka Client 组件在 Eureka Server 注册一个服务消费者 consumer 的具体实现，无论是服务消费者还是服务提供者，都通过 Eureka Client 组件来实现注册，实现服务消费者 consumer 之后，通过 RestTemplate 完成对服务提供者 provider 相关服务的调用。