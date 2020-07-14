# 第18课：Spring Cloud 实例详解——系统发布

title: 19-第18课：Spring Cloud 实例详解——系统发布
date: 2020-07-07 10:43:57
categories: [GitChat,SpringCloud快速入门]
tags: [springcloud]

---

接口开发完成并且测试通过后，就可以进行发布，系统发布可以有很多方式，本文将目前主要的发布方式一一列举出来，供大家参考。

### Java 命令行启动

这种方式比较简单，由于 Spring Boot 默认内置了 Tomcat，我们只需要打包成 Jar，即可通过 Java 命令启动 Jar 包，即我们的应用程序。

首先，news 下面的每个子工程的 pom.xml 中都加入以下内容（Client 除外），表示打包成 Jar 包：

```xml
<packaging>jar</packaging>

<properties>
  <skipTests>true</skipTests>
</properties>

<build>
  <finalName>user</finalName><!-- jar包名称，一般和工程名相同 -->
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
  </plugins>
</build>
```

然后执行打包命令：

```shell
maven clean package
```

第一次运行可能需要花点时间，因为需要从 Maven 仓库下载所有依赖包，以后打包就会比较快，等一段时间后，打包完成：

最后，我们将 Jar 包上传到服务器，依次启动 register.jar、config.jar、gateway.jar、article.jar、comment.jar、index.jar、user.jar 即可，启动命令是：

```shell
nohup java -server -jar xxx.jar &
```

用 nohup 命令启动 Jar 才能使 Jar 在后台运行，否则 shell 界面退出后，程序会自动退出。

### Tomcat 启动

除了 Spring Boot 自带的 Tomcat，我们同样可以自己安装 Tomcat 来部署。

首先改造工程，将所有 `<packaging>jar</packaging>` 改为 `<packaging>war</packaging>`，去掉内置的 Tomcat：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-tomcat</artifactId>
  <scope>provided</scope>
</dependency>
```

然后修改启动类 Application.java：

```java
public class Application extends SpringBootServletInitializer {
  public static void main(String[] args) {
    SpringApplication.run(Application.class,args);
  }
}
```

这样打包后就会生成 War 包，打包方式同上。

我们将 War 上传到服务器的 Tomcat 上即可通过 Tomcat 启动项目。

### Jenkins 自动化部署

> [Jenkins+K8s实现持续集成](https://www.imooc.com/learn/1112)
>
> [用Jenkins自动化搭建测试环境](https://www.imooc.com/learn/1008)

我们搭建的是一套微服务架构，真实环境可能有成百上千个工程，如果都这样手动打包、上传、发布，工作量无疑是巨大的。这时，我们就需要考虑自动化部署了。

Jenkins 走进了我们的视野，它是一个开源软件项目，是基于 Java 开发的一种持续集成工具，用于**监控持续重复的工作**，旨在提供一个开放易用的软件平台，使软件的持续集成变成可能。

下面，我们就来看看如果通过 Jenkins 实现系统的自动化部署。

- 安装
- 创建任务
- 手动构建
- 自动构建

### 总结

系统发布方式很多，我们可以根据自身项目特点选择适合自己的方式，当然还有很多方式，比如 K8S、Docker 等等，这里就不再赘述了 ，关于 K8S+Docker 的方式，我会在第20课讲解。