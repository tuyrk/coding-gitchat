# 服务提供者

title: 05-服务提供者
date: 2020-07-09 16:28:04
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

上节课说到 Eureka Server 是注册中心，分布式系统架构中的所有微服务都需要在注册中心完成注册才能被发现进而使用，我们所说的服务提供者和服务消费者是从业务角度来划分的，实际上服务提供者和服务消费者都是通过 Eureka Client 连接到 Eureka Server 完成注册，本节课我们就来一起实现一个服务提供者，并且在 Eureka Server 完成注册，大致思路是先通过 Spring Boot 搭建一个微服务应用，再通过 Eureka Client 将其注册到 Eureka Server，创建 Eureka Client 的过程与创建 Eureka Server 十分相似，如下所示。

### 代码实现服务提供者Eureka Client

1. 在父工程下创建名为 provider 的 Module，实现 Eureka Client。

2. 在 pom.xml 中添加 Eureka Client 依赖。

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

3. 在 resources 路径下创建配置文件 application.yml，添加 Eureka Client 相关配置，此时的 Eureka Client 是服务提供者 provider

   ```yaml
   server:
     port: 8010 # 当前 Eureka Client 服务端口
   spring:
     application:
       name: provider # 当前服务注册在 Eureka Server 上的名称
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
     instance:
       prefer-ip-address: true # 是否将当前服务的 IP 注册到 Eureka Server
   ```

4. 依次启动注册中心、ProviderApplication，打开浏览器，访问http://localhost:8761

   可以看到服务提供者 provider 已经在 Eureka Server 完成注册，接下来就可以访问 provider 提供的相关服务了

5. 在 provider 服务中提供对 Student 的 CRUD 操作：

   - 创建 Student 类

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

   - 创建管理 Student 对象的接口 StudentRepositoy 及其实现类 StudentRepositoryImpl

     ```java
     public interface StudentRepository {
       public Collection<Student> findAll();
       public Student findById(long id);
       public void saveOrUpdate(Student student);
       public void deleteById(long id);
     }
     ```

   - 创建 StudentHandler
   
     ```java
     @RequestMapping("/student")
     @RestController
     public class StudentHandler {
         @Autowired
         private StudentRepository studentRepository;
         
         @GetMapping("/findAll")
         public Collection<Student> findAll() { }
         @GetMapping("/findById/{id}")
         public Student findById(@PathVariable("id") long id) { }
         @PostMapping("/save")
         public void save(@RequestBody Student student) { }
         @PutMapping("/update")
         public void update(@RequestBody Student student) { }
         @DeleteMapping("/deleteById/{id}")
         public void deleteById(@PathVariable("id") long id) { }
     }
     ```
   
6. 重启 ProviderApplication，通过 Postman 工具测试该服务的相关接口

   ```shell
   curl -X GET http://localhost:8010/student/findAll
   curl -X GET http://localhost:8010/student/findById/1
   curl -X POST http://localhost:8010/student/save -d '{"id":4,"name":"tuyrk","gender":"男"}' -H "Content-Type: application/json"
   curl -X PUT http://localhost:8010/student/update -d '{"id":4,"name":"tyk","gender":"男"}' -H "Content-Type: application/json"
   curl -X DELETE http://localhost:8010/student/deleteById/1
   ```

### 总结
本节课我们讲解了使用 Eureka Client 组件来注册一个服务提供者 provider 的具体实现，不同业务需求下的微服务统一使用 Eureka Client 组件进行注册，我们现在已经实现了一个服务提供者，其他微服务就可以调用它的接口来完成相关业务需求了。