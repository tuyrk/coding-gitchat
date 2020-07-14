# 第08课：服务异常处理

title: 09-第08课：服务异常处理
date: 2020-07-01 15:11:10
categories: [GitChat,SpringCloud快速入门]
tags: [springcloud]

---

上一篇，我们讲了服务之间的相互通信，利用 OpenFeign 的声明式 HTTP 客户端，通过注解的形式很容易做到不同服务之间的相互调用。

我们的服务最终是部署在服务器上，因为各种原因，服务难免会发生故障，那么其他服务去调用这个服务就会调不到，甚至会一直卡在那里，导致用户体验不好。针对这个问题，我们就需要对服务接口做错误处理，一旦发现无法访问服务，则立即返回并报错，我们捕捉到这个异常就可以以可读化的字符串返回到前端。

为了解决这个问题，业界提出了熔断器模型。

### Hystrix 组件

SpringCloud 集成了 Netflix 开源的 Hystrix 组件，该组件实现了熔断器模型，它使得我们很方便地实现熔断器。

在实际项目中，一个请求调用多个服务是比较常见的，如果较底层的服务发生故障将会发生连锁反应。这对于一个大型项目是灾难性的。因此，我们需要利用 Hystrix 组件，当特定的服务不可用达到一个阈值（Hystrix 默认 5 秒 20 次），将打开熔断器，即可避免发生连锁反应。

### 代码实现

紧接上一篇的代码，OpenFeign 是默认自带熔断器的，但是默认关闭的，我们可以在 application.yml 中开启它：

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8081
spring:
  application:
    name: feign
feign:
  hystrix:
    enabled: true # 开启熔断器
```

新建一个类 ApiServiceError.java 并实现 ApiService：

```java
@Component
public class ApiServiceError implements ApiService {
  @Override
  public String index() {
    return "服务发生故障！";
  }
}
```

然后在 ApiService 的注解中指定 fallback：

```java
@FeignClient(value = "eurekaclient", fallback = ApiServiceError.class)
public interface ApiService {
  @RequestMapping(value = "/index", method = RequestMethod.GET)
  String index();
}
```

在 pom.xml 中添加 web 依赖：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

再创建 Controller 类：ApiController，加入如下代码：

```java
@RestController
public class ApiController {
  @Autowired
  private ApiService apiService;

  @RequestMapping("index")
  public String index(){
    return apiService.index();
  }
}
```

### 测试熔断器

分别启动注册中心 EurekaServer、服务提供者 EurekaClient 和服务消费者 Feign，控制台输入以下命令并出现：

```shell
> curl http://localhost:8081/index
Hello World!,端口：8763
```

然后停止 EurekaClient，再次请求，可以看到熔断器生效了：

```
服务发生故障！
```

### 熔断器监控

Hystrix 给我们提供了一个强大的功能，那就是 Dashboard。Dashboard 是一个 Web 界面，它可以让我们监控 Hystrix Command 的响应时间、请求成功率等数据。

下面我们开始改造 feign 工程，在 feign 工程的 pom.xml 下加入依赖：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
```

然后在启动类 Application.java 中加入 `@EnableHystrixDashboard`，并增加一个 Bean 方法：

```java
@SpringCloudApplication
@EnableFeignClients
@EnableHystrixDashboard
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public ServletRegistrationBean getServlet() {
    HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
    ServletRegistrationBean registrationBean = new ServletRegistrationBean<>(streamServlet);
    registrationBean.setLoadOnStartup(1);
    registrationBean.addUrlMappings("/hystrix.stream");
    registrationBean.setName("HystrixMetricsStreamServlet");
    return registrationBean;
  }
}
```

然后分别启动 EurekaServer、EurekaClient 和 Feign 并访问：http://localhost:8081/hystrix，可以看到如下画面：

![enter image description here](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggbhp6kadcj31wv0u0n8n.jpg)

按照上图箭头所示，输入地址信息(http://localhost:8081/hystrix.stream)后，单击 Monitor Stream 按钮进入监控界面，打开新窗口访问：http://localhost:8081/index，在 Dashboard 界面即可看到 Hystrix 监控界面：

![enter image description here](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggbkhcqfbtj323l0u0ah6.jpg)

Hystrix 熔断器的基本用法就介绍到这里。前面我们创建了注册中心(eurekaserver)、服务提供者(eurekaclient)、服务消费者(feign)、服务网关(zuul)和熔断器(hystrix)。每个工程都有配置文件，而且有些配置是相通的，按照这个方式进行应用程序的配置，维护性较差，扩展性也较差，比如很多个服务都会配置数据源，而数据源只有一个，那么如果我们的数据源地址发生变化，所有地方都需要改，如何改进这个问题呢？下一课所讲解的**配置中心**就是为解决这个问题而生的，敬请期待。

