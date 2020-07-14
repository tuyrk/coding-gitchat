# 注册中心和配置中心

上节课我们搭建了 Spring Cloud 实战项目的基本环境，本节课我们来实现注册中心和配置中心。

### 注册中心
> 注册中心是管理调度微服务的核心组件，每个服务提供者或者服务消费者在启动时，会将自己的信息存储在注册中心，服务消费者可以从注册中心查询服务提供者的网络信息，并通过此信息来调用服务提供者的接口。微服务实例与注册中心通过心跳机制完成交互，如果注册中心长时间无法连接某个微服务实例，就会自动销毁该微服务，当某个微服务的网络信息发生变化时，会重新注册。所有的微服务（无论是服务提供者还是服务消费者，包括配置中心）都需要在注册中心进行注册，才能实现调用。

1. 在父工程下创建名为 registrycenter 的 Module

2. 在 pom.xml 中引入 Eureka Server 相关依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
   </dependency>
   ```

3. 在 application.yml 添加 Eureka Server 相关配置

   ```yaml
   server:
     port: 8761
   eureka:
     client:
       register-with-eureka: false
       fetch-registry: false
       service-url:
         defaultZone: http://localhost:8761/eureka/
   ```

4. 在启动类 RegistryCenterApplication 添加注解`@EnableEurekaServer`

### 配置中心

> 配置中心可以对所有微服务的配置文件进行统一管理，便于部署和维护。
> 接下来我们为系统创建配置中心 Config Server，将所有微服务的配置文件统一通过 Git 仓库进行管理。

1. 在父工程下创建名为 configserver 的 Module

2. 在 pom.xml 添加 Spring Cloud Config 相关依赖

   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-config-server</artifactId>
   </dependency>
   ```

3. 在 application.yml 添加 Config Server 相关配置

   ```yaml
   server:
     port: 8888
   spring:
     application:
       name: configserver
     cloud:
       bus:
         trace:
           enable: true
       config:
         server:
           git:
             uri: https://github.com/southwind9801/orderingsystem.git # Git仓库地址
             searchPaths: config # 仓库路径
             username: southwind9801 # Git仓库用户名
             password: southwind9801 # Git仓库密码
         label: master # 仓库的分支
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8761/eureka/
   ```

4. 在启动类 ConfigServerApplication 添加注解`@EnableConfigServer`

### 创建数据库
1. 数据库共 5 张表，分别是：
   - t_admin：保存管理员数据
   - t_user：保存用户数据
   - t_type：保存菜品分类数据
   - t_menu：保存菜品数据
   - t_order：保存订单数据
   
2. SQL 脚本如下：

   ```mysql
   DROP DATABASE IF EXISTS `orderingsystem`;
   CREATE DATABASE `orderingsystem`;
   USE `orderingsystem`;
   
   DROP TABLE IF EXISTS `t_admin`;
    SET character_set_client = utf8mb4 ;
   CREATE TABLE `t_admin` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `username` varchar(11) DEFAULT NULL,
     `password` varchar(11) DEFAULT NULL,
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
   
   DROP TABLE IF EXISTS `t_user`;
    SET character_set_client = utf8mb4 ;
   CREATE TABLE `t_user` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `username` varchar(11) DEFAULT NULL,
     `password` varchar(11) DEFAULT NULL,
     `nickname` varchar(11) DEFAULT NULL,
     `gender` varchar(2) DEFAULT NULL,
     `telephone` varchar(20) DEFAULT NULL,
     `registerdate` date DEFAULT NULL,
     `address` varchar(20) DEFAULT NULL,
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
   
   DROP TABLE IF EXISTS `t_type`;
    SET character_set_client = utf8mb4 ;
   CREATE TABLE `t_type` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `name` varchar(11) DEFAULT NULL,
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
   
   DROP TABLE IF EXISTS `t_menu`;
    SET character_set_client = utf8mb4 ;
   CREATE TABLE `t_menu` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `name` varchar(11) DEFAULT NULL,
     `price` double DEFAULT NULL,
     `flavor` varchar(11) DEFAULT NULL,
     `tid` int(11) DEFAULT NULL,
     PRIMARY KEY (`id`),
     KEY `tid` (`tid`),
     CONSTRAINT `t_menu_ibfk_1` FOREIGN KEY (`tid`) REFERENCES `t_type` (`id`)
   ) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
   
   DROP TABLE IF EXISTS `t_order`;
    SET character_set_client = utf8mb4 ;
   CREATE TABLE `t_order` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `uid` int(11) DEFAULT NULL,
     `mid` int(11) DEFAULT NULL,
     `aid` int(11) DEFAULT NULL,
     `date` date DEFAULT NULL,
     `state` int(11) DEFAULT NULL,
     PRIMARY KEY (`id`),
     KEY `uid` (`uid`),
     KEY `mid` (`mid`),
     KEY `aid` (`aid`),
     CONSTRAINT `t_order_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `t_user` (`id`),
     CONSTRAINT `t_order_ibfk_2` FOREIGN KEY (`mid`) REFERENCES `t_menu` (`id`),
     CONSTRAINT `t_order_ibfk_3` FOREIGN KEY (`aid`) REFERENCES `t_admin` (`id`)
   ) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
   ```


### 总结
本节课我们讲解了实战项目注册中心和配置中心的搭建，同时完成了数据表的创建和数据导入。