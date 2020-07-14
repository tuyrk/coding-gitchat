# Spring Boot 启动原理

title: 02-Spring Boot 启动原理
date: 2020-07-07 18:31:23
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

### Spring Boot 自动配置原理

 Spring Boot 的核心功能是**自动配置**，意思就是 Spring Boot 框架可以**自动读取各种配置文件，并将项目所需要的组件全部加载到 IoC 容器**中，包括**开发者自定义组件**（如 Controller、Service、Repository）以及**框架自带组件**。

那么 Spring Boot 是如何做到自动配置的呢？要探究底层原理，我们应该从哪里入手呢？入口就是 @SpringBootApplication 注解。

@SpringBootApplication 注解实际是由 3 个注解组合而来的，它们分别是：

- @SpringBootConfiguration
- @EnableAutoConfiguration
- @ComponentScan

如下所示，两种配置方式结果是一样的。

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class,args);
  }
}
```

```java
@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class,args);
  }
}
```


要搞懂 Spring Boot 自动配置原理，只需要搞清楚这 3 个注解即可，我们分别来学习。

#### @SpringBootConfiguration

> 该注解其实就是 @Configuration，@Configuration 是 Spring Boot 中使用率非常高的一个注解，作用是标注一个配置类，用 Java 类的形式来替代 XML 的配置方式
>
> @SpringBootConfiguration 注解在这里的作用是将启动类 Application 标注成为一个配置类
- 传统的 XML 配置 bean 的方式

  ```java
  @Data
  @AllArgsConstructor
  public class Account {    
    private String username;    
    private String password;
  }
  ```
  
  ```xml
  <beans>
    <bean id="accout" class="com.southwind.Account">
      <property name="username" value="root"></property>
      <property name="password" value="root"></property>
    </bean>
</beans>
  ```

-   使用 @Configuration 注解的方式

  ```java
  @Configuration
  public class MyConfiguration {
    @Bean(name = "accout")    
    public Account getAccount(){        
      return new Account("root","root");    
    }
  }
  ```

上述两种方式的结果是一样的，都是在 IoC 容器中注入了一个 Account Bean。

#### @ComponentScan

> 该注解的作用是**自动扫描并加载符合条件的组件**，通过设置 **basePackages** 参数的值来指定需要扫描的根目录，该目录下的类及其子目录下的类都会被扫描，默认值为添加了该注解的类所在的包

启动类添加了该注解，那么默认的扫描根目录就是启动类所在的包。如，启动类 Application 所在的包是 com.southwind.springboottest，config、controller、repository 3 个包都是 com.southwind.springboottest 的子包，所以这些类的实例都会被注入到 IoC 容器中

现在给 @ComponentScan 设置 basePackages 参数，修改代码如下所示。

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.southwind.springboottest.controller","com.southwind.springboottest.repository"})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

此时的配置意味着只扫描 com.southwind.springboottest.controller 和 com.southwind.springboottest.repository 这两个包，那么只有这两个包中类的实例会被注入到 IoC 容器中

**@ComponentScan常用参数**如下所示：

> - basePackages：指定需要扫描的根包目录
> - basePackageClasses：指定需要扫描的根类目录
> - lazyInit：是否开启惰性加载，默认关闭
> - useDefaultFilters：是否启用自动扫描组件，默认 true
> - excludeFilters：指定不需要扫描的组件类型
> - includeFilters：指定需要扫描的组件类型

#### @EnableAutoConfiguration

> 该注解是自动配置的核心，非常重要，结合 @Import 注解，将依赖包中相关的 bean 进行注册
>
> @EnableAutoConfiguration 是由两个注解组合而成，分别是
>
> - @AutoConfigurationPackage
>- @Import({AutoConfigurationImportSelector.class})

1. @AutoConfigurationPackage 注解的作用是**自动配置框架自带组件**，该注解其实也是使用了 @Import({Registrar.class})

   ```java
   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   @Inherited
   @Import({Registrar.class})
   public @interface AutoConfigurationPackage {
   }
   ```

   @Import 注解的作用是根据其参数类所返回的信息，将对应的 bean 进行注册，比如这里的参数类是 Registrar.class，Registrar.class 返回信息如下所示：

   ```java
   static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {
     Registrar() {
     }
   
     public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
       AutoConfigurationPackages.register(registry, new String[]{(new AutoConfigurationPackages.PackageImport(metadata)).getPackageName()});
     }
   
     public Set<Object> determineImports(AnnotationMetadata metadata) {
       return Collections.singleton(new AutoConfigurationPackages.PackageImport(metadata));
     }
   }
   ```

   类中方法的作用是将启动类 Application 所在的包下的所有组件注册到 IoC 容器中，即 SpringBoot 默认会扫描启动类所在的包下的所有组件。

2. @Import({AutoConfigurationImportSelector.class}) 完成注册**开发者自定义组件**

   即AutoConfigurationImportSelector.class 返回信息就是框架默认加载的组件，打开 AutoConfigurationImportSelector.class 源码如下所示：

   ```java
   public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered {
       protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
       List<String> configurations = SpringFactoriesLoader.loadFactoryNames(this.getSpringFactoriesLoaderFactoryClass(), this.getBeanClassLoader());
       Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you are using a custom packaging, make sure that file is correct.");
       return configurations;
     }
   }
   ```

   核心代码的是调用 SpringFactoriesLoader.loadFactoryNames 方法从依赖的 jar 包中读取 META-INF/spring.factories 文件中的信息，如下所示。

   ```properties
   # Auto Configure
   org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
   org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
   org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
   org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
   org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
   org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
   org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,\
   org.springframework.boot.autoconfigure.cloud.CloudServiceConnectorsAutoConfiguration,\
   org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration,\
   org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration,\
   org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration,\
   org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration,\
   org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration,\
   org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration,\
   org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration,\
   org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration,\
   org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration,\
   org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataAutoConfiguration,\
   org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration,\
   org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveRepositoriesAutoConfiguration
   ```

   该文件中存在一组 key = org.springframework.boot.autoconfigure.EnableAutoConfiguration 的配置信息，就是要自动配置的 bean，并且对应的都是 @Configuration 类，Spring Boot 通过加载这些配置类，将需要的组件加载到 IoC 容器中。

以上就是 Spring Boot 自动配置的原理。

### 总结
本节课主要为大家讲解了 Spring Boot 的**自动配置原理**、**核心注解的作用**，帮助大家更好地理解 **Spring Boot 的底层机制**。搞清楚框架的底层原理，大家在实际开发中对 Spring Boot 框架的使用才能更加得心应手。