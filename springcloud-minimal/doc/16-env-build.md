# 微服务项目实战：环境搭建

title: 16-微服务项目实战：环境搭建
date: 2020-07-13 15:05:21
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

~~经过前面课程的学习，大家已经对微服务架构的理论有了一定的了解，并且也掌握了 Spring Cloud 相关组件的使用，一切的理论学习都是为了实际应用，通过实践也能更好的去消化理论知识，从本节课开始，我们将一起来~~搭建一个基于微服务架构的外卖点餐系统实际案例。

本节课首先来搭建 Spring Cloud 实战项目的基础环境。

### 项目需求
本项目分为客户端和后台管理系统两个界面：

- 客户端针对普通用户，功能包括用户登录、用户退出、菜品订购、我的订单
- 后台管理系统针对管理员，功能包括管理员登录、管理员退出、添加菜品、查询菜品、修改菜品、删除菜品、订单处理、添加用户、查询用户、删除用户

![1](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggpcso6ut5j316d0u0jy2.jpg)

了解完需求之后，接下来设计系统架构，首先分配出 4 个服务提供者：account、menu、order、user。

- account 提供账户服务：用户和管理员登录。
- menu 提供菜品服务：添加菜品、查询菜品、修改菜品、删除菜品。
- order 提供订单服务：添加订单、查询订单、删除订单、处理订单。
- user 提供用户服务：添加用户、查询用户、删除用户。

接下来分配出 1 个服务消费者，包括客户端的前端页面和后台接口、后台管理系统的前端页面和后台接口，用户/管理员直接访问的资源都保存在服务消费者中，然后服务消费者调用 4 个服务提供者对应的接口完成业务逻辑，并通过 Feign 实现负载均衡。

4 个服务提供者和 1 个服务消费者都需要在注册中心进行注册，同时要注册配置中心，提供远程配置信息读取，服务提供者和服务消费者的配置信息保存在 Git 远程仓库，由配置中心负责拉取。

本系统共由 8 个模块组成，包括注册中心、配置中心、Git 仓库配置信息、服务消费者、4 个服务提供者，关系如下图所示。

![2](https://tva1.sinaimg.cn/large/007S8ZIlgy1ggpcwkre4vj31800u0k8u.jpg)

系统架构搞清楚之后，接下来开始写代码。

### 代码实现

1. 新建名为 orderingsystem的 Maven 工程

2. 在 pom.xml 中引入 Spring Boot 和 Spring Cloud 相关依赖，其中 JAXB API 的依赖只针对 JDK 9 以上版本，如果你是 JDK 9 以下版本，不需要配置。

   ```xml
   <!-- 引入 Spring Boot 的依赖 -->
   <parent>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-parent</artifactId>
     <version>2.0.7.RELEASE</version>
   </parent>
   
   <!-- 引入 Spring Cloud 的依赖，管理 Spring Cloud 生态各个组件 -->
   <dependencyManagement>
     <dependencies>
       <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-dependencies</artifactId>
         <version>Finchley.SR2</version>
         <type>pom</type>
         <scope>import</scope>
       </dependency>
     </dependencies>
   </dependencyManagement>
   
   <dependencies>
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
     <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <optional>true</optional>
     </dependency>
     <!-- 解决 JDK 9 以上版本没有 JAXB API 的问题 -->
     <dependency>
       <groupId>javax.xml.bind</groupId>
       <artifactId>jaxb-api</artifactId>
       <version>2.3.0</version>
     </dependency>
     <dependency>
       <groupId>com.sun.xml.bind</groupId>
       <artifactId>jaxb-impl</artifactId>
       <version>2.3.0</version>
     </dependency>
     <dependency>
       <groupId>com.sun.xml.bind</groupId>
       <artifactId>jaxb-core</artifactId>
       <version>2.3.0</version>
     </dependency>
     <dependency>
       <groupId>javax.activation</groupId>
       <artifactId>activation</artifactId>
       <version>1.1.1</version>
     </dependency>
   </dependencies>
   ```


系统环境搭建完成，从下节课开始我们来实现各种服务提供者。

### 总结
本节课是我们整个 Spring 全家桶课程的最后一个章节，使用 Spring Cloud 实现分布式系统：外卖订餐系统，首先我们讲解了系统的需求，让大家对整个系统有一个直观的认知，然后搭建了 Spring Cloud 的基本环境，后续的课程我们会一步步完善实战项目的开发。

