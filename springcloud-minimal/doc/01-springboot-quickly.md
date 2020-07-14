# Spring Boot 快速入门

title: 01-Spring Boot 快速入门
date: 2020-07-07 18:02:28
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

从本节课开始，我们进入 Spring Boot 框架的学习，Spring Boot 是当前 Java 领域主流的技术栈，同时也是整个 Spring 全家桶中非常重要的一个模块。

#### Spring Boot 简介
Spring 作为一个软件设计层面的框架，在 Java 企业级开发中应用非常广泛，但是 Spring 框架的配置非常繁琐，且大多是重复性的工作，Spring Boot 的诞生就解决了这一问题，通过 Spring Boot 可以快速搭建一个基于 Spring 的 Java 应用程序。Spring Boot 对常用的第三方库提供了配置方案，可以很好地与 Spring 进行整合，如 MyBatis、Spring Data JPA 等，可以一键式搭建功能完备的 Java 企业级应用程序。

#### Spring Boot 的优势：

- 不需要任何 XML 配置文件。
- 内嵌 Web 服务器，可直接部署。
- 默认支持 JSON 数据，不需要额外配置。
- 支持 RESTful 风格。
- 最少一个配置文件可以配置所有的个性化信息。

简而言之，Spring Boot 就是一个用很少的配置就可以快速搭建 Spring 应用的框架，并且很好的集成了常用第三方库，让开发者能够快速进行企业级项目开发。

#### 创建 Spring Boot 工程
有 3 种常用方式可以快速创建一个 Spring Boot 工程，接下来详细给大家介绍每种方式的具体操作。

1. ~~在线创建工程~~
   
   > 打开浏览器访问 https://start.spring.io，可在线创建一个 Spring Boot 工程
   
   - 选择创建工程的方式为Maven、语言为Java，
   
   - Spring Boot 的版本选择 2.1.5，

   - 输入 Group Id 和 Artifact Id，
   
   - 选择需要依赖的模块。
   
   - 点击下方的 Generate 按钮即可下载模版的压缩文件，解压后用 IDEA 打开即可。
   
2. 使用 IDEA Spring Initializr 创建工程
   - 打开 IDEA，选择 Create New Project；
   - 选择 Spring Initializr，点击 Next，可以看到实际上 IDEA 还是通过 https://start.spring.io 来创建工程的；
   - 输入 GroupId、ArtifactId 等基本信息，点击 Next；
   - 选择需要依赖的模块，点击 Next；
   - 选择项目路径，点击 Finish 即可完成创建。

3. ~~手动创建 Spring Boot 工程~~

  - 打开 IDEA，选择 Create New Project；

  - 选择 Maven，点击 Next；

  - 输入 GroupId 和 ArtifactId，点击 Next；

  - 选择项目路径，点击 Finish 即可创建一个空的 Maven 工程。

  - 手动添加 Spring Boot 相关依赖，在 parent 标签中配置 spring-boot-starter-parent 的依赖，相当于给整个工程配置了一个 Spring Boot 的父依赖，其他模块直接在继承父依赖的基础上添加特定依赖即可。

    比如现在要集成 Web MVC 组件，直接在 dependencies 中添加一个 spring-boot-starter-web 依赖即可，默认使用 Tomcat 作为 Web 容器，pom.xml 如下所示。

    ```xml
    <parent>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-parent</artifactId>
      <version>2.1.5.RELEASE</version>
    </parent>
    
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
    </dependencies>
    ```

#### 使用 Spring Boot

通过以上任意一种方式都可快速搭建一个 Spring Boot 工程，然后就可以根据需求添加各种子模块依赖了，比如上述的第 3 种方式，我们添加了 Web MVC 组件，当前工程就成为了一个 Spring MVC 框架项目，开发者可以按照 Spring MVC 的开发步骤直接写代码了，同时 Spring Boot 还帮助我们大大简化了配置文件。

你可以拿当前的工程和之前课程中我们创建的 Spring MVC 工程对比一下，会发现**不需要在 web.xml 中配置 DispatcherServlet**，同时也**不需要创建 springmvc.xml** 了。

传统 Spring MVC 工程的 springmvc.xml 中主要添加三个配置，一是**启用注解驱动**，二是**自动扫包**，三是**视图解析器**。Spring Boot 自动帮我们搞定了前两个配置，第三个视图解析器需要开发者手动配置，因为视图层资源的存储路径和文件类型框架是没有办法自动获取的，不同工程的具体方式也不一样，像这种个性化的配置，Spring Boot 框架是无法自动完成的，需要开发者在 Spring Boot 特定的配置文件中自己完成。

好了，接下来我们就一起来学习用 Spring Boot 启动 Web 应用的具体操作。

1. 创建 HelloHandler，具体步骤与 Spring MVC 一样

   ```java
   @RestController
   public class HelloHandler {
     @GetMapping("/index")
     public String index() {
       return "Hello World";
     }
   }
   ```

2. 创建 Spring Boot 启动类 Application

   ```java
   @SpringBootApplication
   public class Application {
     public static void main(String[] args) {
       SpringApplication.run(Application.class,args);
     }
   }
   ```

   这个类是整个 Spring Boot 应用的入口，可以看到在类定义处添加了一个 @SpringBootApplication 注解，这个注解是 Spring Boot 的核心，它**开启了 Spring Boot 的自动化配置**，开发者可以使用自定义配置来覆盖自动化配置，同时它完成了**自动扫包**，默认的范围是该类所在包的所有子包，当然也包括所在包本身，因此我们在实际开发中应该将启动类放在跟目录下。

3. 启动 Spring Boot 应用，直接运行启动类的 main 方法即可，会自动将项目部署到内置 Tomcat 中，并启动 Tomcat

   启动成功，并且默认端口为 8080，控制台运行```curl http://localhost:8080/index```，打印```Hello World```

应用启动后会自动进行扫描，将需要的类交给 Spring IoC 容器来管理，被扫描的类需要在 Application 同级或子级包下

### ~~总结~~
本节课作为整个 Spring Boot 阶段的开篇课程，我们主要讲解了 Spring Boot 的基本概念，Spring Boot 应用的创建方式，以及 Spring Boot 的基本使用。相信大家已经可以感受到使用 Spring Boot 搭建一个 Spring 应用是多么简单快捷，后续的课程会在此基础上讲解更多 Spring Boot 技术栈的实用模块。
