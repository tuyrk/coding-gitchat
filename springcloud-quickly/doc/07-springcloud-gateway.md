# 第06课：服务网关

本文，我们将学习 Spring Cloud 的另一个组件：gateway，它提供**微服务的网关功能**，**即中转站**，通过它提供的接口，可以**转发不同的服务**。在学习 gateway之前，我们先接着上一篇的代码，来看看服务提供者是如何提供服务的。

在服务提供者的 module 下创建 HelloController 类，添加内容如下：

```java
@RestController
public class HelloController {
  @Value("${server.port}")
  private int port;

  @RequestMapping("index")
  public String index(){
    return "Hello World!,端口：" + port;
  }
}
```

然后分别启动服务注册中心和服务提供者，控制台输入以下命令并出现：

```shell
> curl http://localhost:8762/index
Hello World!,端口：8762
```



在实际的项目中，一个项目可能会包含很多个服务，**每个服务的端口和 IP 都可能不一样**。那么，如果我们以这种形式提供接口给外部调用，代价是非常大的。从安全性上考虑，系统**对外提供的接口应该进行合法性校验，防止非法请求**，如果按照这种形式，那每个服务都要写一遍校验规则，维护起来也很麻烦。

这个时候，我们需要**统一的入口，接口地址全部由该入口进入，而服务只部署在局域网内供这个统一的入口调用**，这个入口就是我们通常说的**服务网关**。

Spring Cloud 给我们提供了这样一个解决方案，那就是 zuul，它的作用就是进行**路由转发、异常处理和过滤拦截**。下面，我将演示如何使用 zuul 创建一个服务网关。

### 创建 gateway 工程

在父项目上创建一个名为 gateway 的工程，加入maven依赖：

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
</dependencies>
```

**Spring Cloud Gateway 依赖 WebFlux**，因此这里还需要加入 WebFlux 依赖。

hystrix 为熔断器依赖。

添加 application.yml 配置文件，内容如下：

```yaml
server:
  port: 8080
spring:
  application:
    name: gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true # 是否与服务发现组件（register）进行结合
logging:
  level: # 日志配置策略
    org.springframework.cloud.gateway: trace
    org.springframework.http.server.reactive: debug
    org.springframework.web.reactive: debug
    reactor.ipc.netty: debug
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

服务网关的配置多了几项，具体含义如下。

spring.cloud.gateway.discovery.locator.enabled：表示是否与服务发现组件（register）进行结合，通过 serviceId（必须设置成大写）转发到具体的服务实例。默认为 false，设为 true 便开启通过服务中心的自动根据 serviceId 创建路由的功能。路由访问方式：```http://Gateway_HOST:Gateway_PORT/ 大写的 serviceId/**```，其中微服务应用名默认大写访问。 logging.level：日志配置策略。

然后我们启动服务注册中心、服务提供者、服务网关，控制台输入以下命令并出现：

```shell
> curl http://localhost:8080/EUREKACLIENT/index
Hello World!,端口：8762
```

### 服务拦截

前面我们提到，**服务网关**还有个作用就是**接口的安全性校验**，这个时候就需要通过 gateway 进行统一拦截。Gateway 提供了多种 Filter 可供选择，如 GatewayFilter、GlobalFilter 等，不同的 Filter 的作用是不一样的，**GatewayFilter 处理单个路由的请求**，而 **GlobalFilter** 根据名字大致就能知道其作用，它是一个全局 Filter，可以**过滤所有路由请求**，本课以 GlobalFilter 为例，讲解如何通过 Filter 过滤 API 请求，从而达到鉴权的目的。

新建一个类 ApiGlobalFilter 并继承 GlobalFilter：

```java
@Component
public class ApiGlobalFilter implements GlobalFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String token = exchange.getRequest().getQueryParams().getFirst("token");
    if (StringUtils.isBlank(token)) {
      ServerHttpResponse response = exchange.getResponse();
      Map<String,Object> message = new HashMap<>();
      message.put("status", -1);
      message.put("data", "鉴权失败");
      byte[] bits = message.toString().getBytes(StandardCharsets.UTF_8);
      DataBuffer buffer = response.bufferFactory().wrap(bits);
      response.setStatusCode(HttpStatus.UNAUTHORIZED);
      response.getHeaders().add("Content-Type", "text/json;charset=UTF-8");
      return response.writeWith(Mono.just(buffer));
    }
    return chain.filter(exchange);
  }
}
```

通过重写 filter 方法，由于 Gateway 依赖 WebFlux，因此这里也返回的是 Mono 对象，通过 Mono 返回数据到前端，上述代码，我们首先取得 token 参数，判断 token 是否存在，如果不存在则返回鉴权失败，这只是一个最简单的鉴权例子，具体的鉴权实现根据具体的项目来考虑。

启动 gateway，控制台输入以下命令并出现：

```shell
> curl http://localhost:8080/EUREKACLIENT/index
{data=鉴权失败, status=-1}
> curl http://localhost:8080/EUREKACLIENT/index\?token\=12345
Hello World!,端口：8762
```

### 错误拦截

在一个大型系统中，服务是部署在不同的服务器下面的，我们难免会遇到某一个服务挂掉或者请求不到的时候，如果不做任何处理，服务网关请求不到会抛出 500 错误，对用户是不友好的。

我们为了提供用户的友好性，需要返回友好性提示，Spring Cloud Gateway 为我们提供了一个名叫 DefaultErrorWebExceptionHandler 的类，通过继承它我们就可以对这些请求不到的服务进行错误处理。

新建一个类 JsonExceptionHandler 并且继承 DefaultErrorWebExceptionHandler 类：

```java
/**
 * 自定义异常处理
 * SpringBoot 提供了默认的异常处理类，这显然不符合我们的预期
 * 因此需要重写此类，返回统一的 JSON 格式
 */
public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {
  public JsonExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                              ErrorProperties errorProperties, ApplicationContext applicationContext) {
    super(errorAttributes, resourceProperties, errorProperties, applicationContext);
  }

  // 获取异常属性
  @Override
  protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
    int code = 500;
    Throwable error = super.getError(request);
    if (error instanceof org.springframework.cloud.gateway.support.NotFoundException) {
      code = 404;
    }
    return response(code, this.buildMessage(request, error));
  }

  // 指定响应处理方法为 JSON 处理的方法
  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  // 根据 code 获取对应的 HttpStatus
  @Override
  protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
    int statusCode = (int) errorAttributes.get("code");
    return HttpStatus.valueOf(statusCode);
  }

  // 构建异常信息
  private String buildMessage(ServerRequest request, Throwable ex) {
    StringBuilder message = new StringBuilder("Failed to handle request [");
    message.append(request.methodName());
    message.append(" ");
    message.append(request.uri());
    message.append("]");
    if (ex != null) {
      message.append(": ");
      message.append(ex.getMessage());
    }
    return message.toString();
  }

  // 构建返回的 JSON 数据格式
  public static Map<String, Object> response(int status, String errorMessage) {
    Map<String, Object> map = new HashMap<>();
    map.put("code", status);
    map.put("message", errorMessage);
    map.put("data", null);
    return map;
  }
}
```

我们还需要将上述类加载到 Spring 容器中，因此还需要添加以下配置类：

```java
@SpringBootConfiguration
@EnableConfigurationProperties({ServerProperties.class, ResourceProperties.class})
public class ErrorHandlerConfiguration {
  private final ServerProperties serverProperties;
  private final ApplicationContext applicationContext;
  private final ResourceProperties resourceProperties;
  private final List<ViewResolver> viewResolvers;
  private final ServerCodecConfigurer serverCodecConfigurer;
  public ErrorHandlerConfiguration(ServerProperties serverProperties,
                                   ResourceProperties resourceProperties,
                                   ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                   ServerCodecConfigurer serverCodecConfigurer,
                                   ApplicationContext applicationContext) {
    this.serverProperties = serverProperties;
    this.applicationContext = applicationContext;
    this.resourceProperties = resourceProperties;
    this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
    this.serverCodecConfigurer = serverCodecConfigurer;
  }

  // gateway 启动时执行此方法，将 JsonExceptionHandler 注入到 Spring 容器中，以便发生异常时执行自定义的 JsonExceptionHandler
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes) {
    JsonExceptionHandler exceptionHandler = new JsonExceptionHandler(
      errorAttributes,
      this.resourceProperties,
      this.serverProperties.getError(),
      this.applicationContext);
    exceptionHandler.setViewResolvers(this.viewResolvers);
    exceptionHandler.setMessageWriters(this.serverCodecConfigurer.getWriters());
    exceptionHandler.setMessageReaders(this.serverCodecConfigurer.getReaders());
    return exceptionHandler;
  }
}
```

现在开始测试这部分代码，首先停掉服务提供者 eurekaclient，再重启 gateway，控制台输入以下命令并出现：

```shell
> curl http://localhost:8080/EUREKACLIENT/index\?token\=12345
{"code":500,"data":null,"message":"Failed to handle request [GET http://localhost:8090/EUREKACLIENT/index?token=12345]: Connection refused: tuyrk.local/127.0.0.1:8762"}
```

