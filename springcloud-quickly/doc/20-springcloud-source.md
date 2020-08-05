# 第19课：Spring Cloud 源码解析

Spring Cloud 集成了很多第三方框架，把它的全部源码拿出来解析几本书都讲不完，也不太现实，本文带领读者分析其中一小部分源码（其余源码读者有兴趣可以继续跟进），包括 Eureka-Server、Config、~~Zuul~~ 的 starter 部分，分析其启动原理。

如果我们开发出一套框架，要和 Spring Boot 集成，就需要放到它的 starter 里。因此我们分析启动原理，直接从每个框架的 starter 开始分析即可。

### Eureka-Server 源码解析

我们知道，要实现注册与发现，需要在启动类加上 `@EnableEurekaServer` 注解，我们进入其源码：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EurekaServerMarkerConfiguration.class)
public @interface EnableEurekaServer {
}
```

注意看 `@Import` 注解，这个注解导入了 EurekaServerMarkerConfiguration 类，继续跟进这个类：

```java
/**
 * Responsible for adding in a marker bean to activate
 * {@link EurekaServerAutoConfiguration}
 */
{@link EurekaServerAutoConfiguration}
@Configuration
public class EurekaServerMarkerConfiguration {
  @Bean
  public Marker eurekaServerMarkerBean() { return new Marker(); }

  class Marker { }
}
```

通过上面的注释，我们继续查看 [EurekaServerAutoConfiguration](https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-eureka-server/src/main/java/org/springframework/cloud/netflix/eureka/server/EurekaServerAutoConfiguration.java) 类的源码：

```java
@Configuration
@Import(EurekaServerInitializerConfiguration.class)
@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties({ EurekaDashboardProperties.class,
		InstanceRegistryProperties.class })
@PropertySource("classpath:/eureka/server.properties")
public class EurekaServerAutoConfiguration extends WebMvcConfigurerAdapter {
}
```

这个类上有一个注解：`@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)`，这后面指定的类就是刚才那个类，而 `@ConditionalOnBean` 这个注解的作用是：仅仅在当前上下文中存在某个对象时，才会实例化一个 Bean。

因此，启动时就会实例化 EurekaServerAutoConfiguration 这个类。

```
@EnableConfigurationProperties({ EurekaDashboardProperties.class,
        InstanceRegistryProperties.class })
```

这个注解就是定义了一些 Eureka 的配置项。

### Config 源码解析

通过上面的方法，我们找到了 ConfigServerAutoConfiguration 类：

```java
@Configuration
@ConditionalOnBean(ConfigServerConfiguration.Marker.class)
@EnableConfigurationProperties(ConfigServerProperties.class)
@Import({ EnvironmentRepositoryConfiguration.class, CompositeConfiguration.class, ResourceRepositoryConfiguration.class,
        ConfigServerEncryptionConfiguration.class, ConfigServerMvcConfiguration.class, TransportConfiguration.class })
public class ConfigServerAutoConfiguration {
}
```

可以发现这个类是空的，只是多了几个注解， `@EnableConfigurationProperties(ConfigServerProperties.class)` 表示开启 Config 配置属性。

最核心的注解是：`@Import`，它将其他一些配置类导入这个类，其中， [EnvironmentRepositoryConfiguration](https://github.com/spring-cloud/spring-cloud-config/blob/master/spring-cloud-config-server/src/main/java/org/springframework/cloud/config/server/config/EnvironmentRepositoryConfiguration.java) 为环境配置类，内置了以下几种环境配置。

1. Native

   ```java
   @Configuration(proxyBeanMethods = false)
   @Profile("native")
   class NativeRepositoryConfiguration {
     @Bean
     public NativeEnvironmentRepository nativeEnvironmentRepository(
       NativeEnvironmentRepositoryFactory factory,
       NativeEnvironmentProperties environmentProperties) {
       return factory.build(environmentProperties);
     }
   }
   ```

2. git

   ```java
   @Configuration(proxyBeanMethods = false)
   @Profile("git")
   class GitRepositoryConfiguration extends DefaultRepositoryConfiguration {
   }
   ```

3. subversion

   ```java
   @Configuration(proxyBeanMethods = false)
   @Profile("subversion")
   class SvnRepositoryConfiguration {
     @Bean
     public SvnKitEnvironmentRepository svnKitEnvironmentRepository(
       SvnEnvironmentRepositoryFactory factory,
       SvnKitEnvironmentProperties environmentProperties) {
       return factory.build(environmentProperties);
     }
   }
   ```

4. vault

   ```java
   @Configuration(proxyBeanMethods = false)
   @ConditionalOnClass(VaultTemplate.class)
   @Profile("vault")
   class SpringVaultRepositoryConfiguration {
     @Bean
     public SpringVaultEnvironmentRepository vaultEnvironmentRepository(
       SpringVaultEnvironmentRepositoryFactory factory,
       VaultEnvironmentProperties environmentProperties) {
       return factory.build(environmentProperties);
     }
   }
   ```

从代码可以看到 Git 是配置中心默认环境。

```java
@Bean
public MultipleJGitEnvironmentRepository defaultEnvironmentRepository(
  MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory,
  MultipleJGitEnvironmentProperties environmentProperties) throws Exception {
  return gitEnvironmentRepositoryFactory.build(environmentProperties);
}
```

我们进入 [MultipleJGitEnvironmentRepository](https://github.com/spring-cloud/spring-cloud-config/blob/master/spring-cloud-config-server/src/main/java/org/springframework/cloud/config/server/environment/MultipleJGitEnvironmentRepository.java) 类：

```java
public class MultipleJGitEnvironmentRepository extends JGitEnvironmentRepository {
}
```

这个类表示可以支持配置多个 Git 仓库，它继承自 [JGitEnvironmentRepository](https://github.com/spring-cloud/spring-cloud-config/blob/master/spring-cloud-config-server/src/main/java/org/springframework/cloud/config/server/environment/JGitEnvironmentRepository.java) 类：

```java
public class JGitEnvironmentRepository extends AbstractScmEnvironmentRepository implements EnvironmentRepository, SearchPathLocator, InitializingBean {
  /**
   * Get the working directory ready.
   */
  public String refresh(String label) {
  }
}
```

refresh 方法的作用就是 ConfigServer 会从我们配置的 Git 仓库拉取配置下来。

### ~~Zuul 源码解析~~

同理，我们找到 Zuul 的配置类 [ZuulProxyAutoConfiguration](https://github.com/spring-cloud/spring-cloud-netflix/blob/2.2.x/spring-cloud-netflix-zuul/src/main/java/org/springframework/cloud/netflix/zuul/ZuulProxyAutoConfiguration.java)：

```java
@Configuration
@Import({ RibbonCommandFactoryConfiguration.RestClientRibbonConfiguration.class,
         RibbonCommandFactoryConfiguration.OkHttpRibbonConfiguration.class,
         RibbonCommandFactoryConfiguration.HttpClientRibbonConfiguration.class })
@ConditionalOnBean(ZuulProxyMarkerConfiguration.Marker.class)
public class ZuulProxyAutoConfiguration extends ZuulServerAutoConfiguration {
}
```

通过 `@Import` 注解可以找到几个类：

- RibbonCommandFactoryConfiguration.RestClientRibbonConfiguration
- RibbonCommandFactoryConfiguration.OkHttpRibbonConfiguration
- RibbonCommandFactoryConfiguration.HttpClientRibbonConfiguration

我们知道 Zuul 提供网关能力，通过上面这几个类就能分析到，它内部其实也是通过接口请求，找到每个服务提供的接口地址。

进入 [RibbonCommandFactoryConfiguration](https://github.com/spring-cloud/spring-cloud-netflix/blob/2.2.x/spring-cloud-netflix-zuul/src/main/java/org/springframework/cloud/netflix/zuul/RibbonCommandFactoryConfiguration.java) 类：

```java
public class RibbonCommandFactoryConfiguration {
}
```

### 总结

前面带领大家分析了一小段源码，Spring Cloud 很庞大，不可能一一分析，本文的主要目的就是教大家如何分析源码，从何处下手，以便大家可以按照这种思路继续跟踪下去。