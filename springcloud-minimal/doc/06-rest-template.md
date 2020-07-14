# 跨服务接口调用神器 RestTemplate

title: 06-跨服务接口调用神器 RestTemplate
date: 2020-07-09 17:05:48
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

在实现服务消费者之前，我们先来学习 RestTemplate 的使用，通过 RestTemplate 可以实现不同微服务之间的调用。

### 什么是 REST

REST 是当前比较流行的一种互联网软件架构模型，通过**统一的规范完成不同终端的数据访问和交互**，REST 是一个词组的缩写，全称为 Representational State Transfer，翻译成中文的意思是资源表现层状态转化。

#### 特点

1. URL 传参更加简洁，如下所示：
   - 非 RESTful 的 URL：`http://…../queryUserById?id=1`
   - RESTful 的 URL：`http://…./queryUserById/1`

2. 完成不同终端之间的资源共享，RESTful 提供了一套规范，不同终端之间只需要遵守该规范，就可以实现数据交互。

RESTful 具体来讲就是四种表现形式，HTTP 协议中四种请求类型（GET、POST、PUT、DELETE）分别表示四种常规操作，即 CRUD：GET 获取资源、POST 创建资源、PUT 修改资源、DELETE 删除资源

### 什么是 RestTemplate

RestTemplate 是 Spring 框架提供的基于 REST 的服务组件，底层对 HTTP 请求及响应进行了封装，提供了很多访问远程 REST 服务的方法，可简化代码开发。

### 如何使用 RestTemplate

> 使用 RestTemplate 来访问搭建好的 REST 服务

1. 实例化 RestTemplate 对象并通过 @Bean 注入 IoC

   ```java
   @Bean
   public RestTemplate createRestTemplate(){
     return new RestTemplate();
   }
   ```

2. 创建 RestHandler 类，通过 @Autowired 将 IoC 容器中的 RestTemplate 实例对象注入 RestHandler，在业务方法中就可以通过 RestTemplate 来访问 REST 服务了。

   ```java
   @RestController
   @RequestMapping("/rest")
   public class RestHandler {
     @Autowired
     private RestTemplate restTemplate;
   }
   ```

3. 调用 RestTemplate 的相关方法，分别通过 GET、POST、PUT、DELETE 请求，访问服务资源。

   > url 为请求的目标资源
   > request 为要保存/修改的目标对象
   > responseType 为响应数据的封装模版
   > uriVariables 是一个动态参数，可以根据实际请求传入参数

   #### GET

   - getForEntity() 方法的返回值类型为 ResponseEntity，通过调用其 getBody 方法可获取结果对象

     ```java
     public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
       RequestCallback requestCallback = this.acceptHeaderRequestCallback(responseType);
       ResponseExtractor<ResponseEntity<T>> responseExtractor = this.responseEntityExtractor(responseType);
       return (ResponseEntity)nonNull(this.execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables));
     }
     ```

     ```java
     @GetMapping("/findAll")
     public Collection<Student> findAll() {
       return restTemplate.getForEntity("http://localhost:8010/student/findAll", Collection.class).getBody();
     }
     ```

     上述代码表示访问 http://localhost:8080/user/findAll，并将结果封装为一个 Collection 对象。

     如果需要传参，直接将参数追加到 getForEntity 方法的参数列表中即可，如下所示。

     ```java
     @GetMapping("/findById/{id}")
     public Student findById(@PathVariable("id") Long id) {
       return restTemplate.getForEntity("http://localhost:8010/student/findById/{id}", Student.class, id).getBody();
     }
     ```

   - getForObject 方法的使用与 getForEntity 很类似，唯一的区别在于 getForObject 的返回值就是目标对象，无需通过调用 getBody 方法来获取

     ```java
     @Nullable
     public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
       RequestCallback requestCallback = this.acceptHeaderRequestCallback(responseType);
       HttpMessageConverterExtractor<T> responseExtractor = new HttpMessageConverterExtractor(responseType, this.getMessageConverters(), this.logger);
       return this.execute(url, HttpMethod.GET, requestCallback, responseExtractor, (Object[])uriVariables);
     }
     ```

     ```java
     @GetMapping("/findAll2")
     public Collection<Student> findAll2() {
       return restTemplate.getForObject("http://localhost:8010/student/findAll", Collection.class);
     }
     ```

     如果需要传参，直接将参数追加到 getForObject 方法的参数列表中即可，如下所示。

     ```java
     @GetMapping("/findById2/{id}")
     public Student findById2(@PathVariable("id") Long id) {
       return restTemplate.getForObject("http://localhost:8010/student/findById/{id}", Student.class, id);
     }
     ```

   #### POST

   - postForEntity 方法的返回值类型也是 ResponseEntity，通过调用其 getBody 方法可获取结果对象

     ```java
     public <T> ResponseEntity<T> postForEntity(String url, @Nullable Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
       RequestCallback requestCallback = this.httpEntityCallback(request, responseType);
       ResponseExtractor<ResponseEntity<T>> responseExtractor = this.responseEntityExtractor(responseType);
       return (ResponseEntity)nonNull(this.execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables));
     }
     ```

     ```java
     @PostMapping("/save")
     public Collection<Student> save(@RequestBody Student student) {
       return restTemplate.postForEntity("http://localhost:8010/student/save", student, Collection.class).getBody();
     }
     ```

   - postForObject 方法的使用与 postForEntity 类似，唯一的区别在于 postForObject 的返回值就是目标对象，无需通过调用 getBody 方法来获取

     ```java
     @Nullable
     public <T> T postForObject(String url, @Nullable Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
       RequestCallback requestCallback = this.httpEntityCallback(request, responseType);
       HttpMessageConverterExtractor<T> responseExtractor = new HttpMessageConverterExtractor(responseType, this.getMessageConverters(), this.logger);
       return this.execute(url, HttpMethod.POST, requestCallback, responseExtractor, (Object[])uriVariables);
     }
     ```

     ```java
     @PostMapping("/save2")
     public Collection<Student> save2(@RequestBody Student student) {
       return restTemplate.postForObject("http://localhost:8010/student/save", student, Collection.class);
     }
     ```

   #### PUT

   ```java
   public void put(String url, @Nullable Object request, Object... uriVariables) throws RestClientException {
     RequestCallback requestCallback = this.httpEntityCallback(request);
     this.execute(url, HttpMethod.PUT, requestCallback, (ResponseExtractor)null, (Object[])uriVariables);
   }
   ```

   ```java
   @PutMapping("/update")
   public void update(@RequestBody Student student) {
     restTemplate.put("http://localhost:8010/student/update", student);
   }
   ```

   #### DELETE

   ```java
   public void delete(String url, Object... uriVariables) throws RestClientException {
      this.execute(url, HttpMethod.DELETE, (RequestCallback)null, (ResponseExtractor)null, (Object[])uriVariables);
   }
   ```

   ```java
   @DeleteMapping("/deleteById/{id}")
   public void delete(@PathVariable("id") Long id) {
     restTemplate.delete("http://localhost:8010/student/deleteById/{id}", id);
   }
   ```

4. 重启 ProviderApplication，通过 Postman 工具测试该服务的相关接口

   ```shell
   curl -X GET http://localhost:8010/rest/findAll
   curl -X GET http://localhost:8010/rest/findById/1
   curl -X POST http://localhost:8010/rest/save -d '{"id":4,"name":"tuyrk","gender":"男"}' -H "Content-Type: application/json"
   curl -X PUT http://localhost:8010/rest/update -d '{"id":4,"name":"tyk","gender":"男"}' -H "Content-Type: application/json"
   curl -X DELETE http://localhost:8010/rest/deleteById/1
   ```

### 总结

本节课我们讲解了 RestTemplate 的使用，RestTemplate 底层对 HTTP 请求及响应进行了封装，提供了很多访问远程 REST 服务的方法，基于它的这个特性，我们可以实现不同微服务之间的调用。下节课我们将一起来实现服务消费者 consumer，并通过 RestTemplate 来调用服务提供者 provider 的相关接口。

[点击这里获取 Spring Cloud 视频专题](https://pan.baidu.com/s/1P_3n6KnPdWBFnlAtEdTm2g)，提取码：yfq2
