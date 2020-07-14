# Spring Cloud Config 远程配置

前面的课程我们学习了本地 Config Server 的搭建方式，本节课我们一起学习远程 Config Server 的环境搭建，即将各个微服务的配置文件放置在远程 Git 仓库中，通过 Config Server 进行统一管理。本课程中我们使用基于 Git 的第三方代码托管远程仓库 GitHub 作为远程仓库，实际开发中也可以使用 Gitee、SVN 或者自己搭建的私服作为远程仓库，Config Server 结构如下图所示。

![](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggp468m4i8j31ej0u013b.jpg)

接下来我们就来一起搭建远程 Config Server。

### GitHub 远程配置文件

> 首先将配置文件上传到 GitHub 仓库

1. 在父工程下创建文件夹 config，config 中创建 configclient.yml。

2. configclient.yml 中配置客户端相关信息。

   ```yaml
   server:
     port: 8070
   spring:
     application:
       name: configclient
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8761/eureka/
   management:
     security:
       enabled: false
   ```

3. 将 config 上传至 GitHub，作为远程配置文件。

### 创建 Config Server

1. 在父工程下创建名为 configserver 的 Module

2. 在 pom.xml 中添加 Spring Cloud Config 依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-config-server</artifactId>
   </dependency>
   ```

3. 在 resources 路径下创建配置文件 application.yml，添加 Config Server 相关配置。

   ```yaml
   server:
     port: 8888 # 当前 Config Server 服务端口
   spring:
     application:
       name: configserver # 当前服务注册在 Eureka Server 上的名称
     cloud:
       config:
         server:
           git: # Git 仓库配置文件信息
             uri: https://github.com/tuyrk/myspringclouddemo.git # Git Repository 地址
             searchPaths: config # 配置文件路径
             username: root # 访问 Git Repository 的用户名
             password: root # 访问 Git Repository 的密码
         label: master # Git Repository 的分支
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
   ```

4. 在启动类 ConfigServerApplication 添加注解

   ```java
   @EnableConfigServer // 声明配置中心
   ```

远程 Config Server 环境搭建完成，接下来创建 Config Client，读取远程配置中心的配置信息。

### 创建 Config Client

1. 在父工程下创建名为 configclient 的 Module

2. 在 pom.xml 中添加 Eureka Client、Spring Cloud Config 依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-config</artifactId>
   </dependency>
   ```

3. 在 resources 路径下新建 bootstrap.yml，配置读取远程配置中心的相关信息。

```yaml
spring:
  cloud:
    config:
      name: configclient # 当前服务注册在 Eureka Server 上的名称，与远程 Git 仓库的配置文件名对应
      label: master # Git Repository 的分支
      discovery:
        enabled: true # 是否开启 Config 服务发现支持
        serviceId: configserver # 配置中心的名称
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/ # 注册中心的访问地址
```

4. 创建 HelloHandler，定义相关业务方法。

   ```java
   @RequestMapping("/config")
   @RestController
   public class HelloHandler {
     @Value("${server.port}")
     private int port;
   
     @RequestMapping(value = "/index")
     public String index() {
       return "当前端口：" + this.port;
     }
   }
   ```

5. 依次启动注册中心、configserver、configclient。

   通过控制台输出信息可以看到，configclient 已经读取到了 Git 仓库中的配置信息。

6. 访问请求服务方法

   ```shell
   curl -X GET http://localhost:8070/config/index
   ```

### 动态更新

如果此时对远程配置中心的配置文件进行修改，微服务需要重启以读取最新的配置信息，实际运行环境中这种频繁重启服务的方式是需要避免的，我们可以通过动态更新的方式，实现**在不重启服务的前提下自动更新配置信息**的功能。

动态更新的实现需要借助于 Spring Cloud Bus 来完成，**Spring Cloud Bus 是一个轻量级的分布式通信组件**，它的**原理是将各个微服务组件与消息代理进行连接，当配置文件发生改变时，会自动通知相关微服务组件，从而实现动态更新**，具体实现如下。

1. 修改 Config Server 的 application.yml，添加 RabbitMQ

   ```yaml
   spring:
     cloud:
       bus:
         trace:
           enable: true
     rabbitmq:
       host: localhost
       port: 5672
       username: guest
       password: guest
   management:
     endpoints:
       web:
         exposure:
           include: bus-refresh
   ```

2. 修改 Config Client 的 pom.xml，添加 actuator、bus-amqp 依赖。

   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-bus-amqp</artifactId>
   </dependency>
   ```

3. 修改 Config Client 的 bootstrap.yml，添加 rabbitmq、bus-refresh

   ```yaml
   spring:
     rabbitmq:
       host: localhost
       port: 5672
       username: guest
       password: guest
   management:
     endpoints:
       web:
         exposure:
           include: bus-refresh
   ```

4. 修改 HelloHandler，添加 @RefreshScope 注解

5. 修改 config 中的配置文件，将端口改为 8080，并更新到 GitHub

6. 在不重启服务的前提下，实现配置文件的动态更新，启动 RabbitMQ

7. 发送 POST 请求，访问 http://localhost:8070/actuator/bus-refresh

   ```shell
   curl -X POST http://localhost:8070/actuator/bus-refresh
   ```

8. 这样就实现动态更新了，再来访问 http://localhost:8070/config/index，可以看到端口已经更新为 8080

   ```shell
   curl -X GET http://localhost:8070/config/index
   ```

9. 设置 GitHub 自动推送更新，添加 Webhooks，如下图所示

   > Settings -> Webhooks -> Add webhook

   ![15](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggp46e43cgj31k80s0gqd.jpg)

10. 在 Payload URL 输入你的服务地址，如 http://localhost:8070/actuator/bus-refresh，注意将 localhost 替换成服务器的外网 IP。

### 总结

本节课我们讲解了使用 Spring Clound Config 来实现远程配置中心的具体操作，使用 Git 存储配置信息，每次修改配置信息后都需要重启各种微服务，非常麻烦，Spring Cloud 提供了自动刷新的解决方案，在不重启微服务的情况下，通过 RabbitMQ 来完成配置信息的自动更新。