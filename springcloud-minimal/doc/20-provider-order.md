# 服务提供者 order

title: 20-服务提供者 order
date: 2020-07-14 10:55:54
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

本节课我们来实现服务提供者 orde，order 为系统提供订单相关服务，包括添加订单、查询订单、删除订单、处理订单，具体实现如下所示。

1. 在父工程下创建建名为 order 的 Module，pom.xml 添加相关依赖

   order 配置文件从 Git 仓库拉取，所以需要添加 Spring Cloud Config 相关依赖；同时需要访问数据库，因此还要添加 MyBatis 相关依赖。

   ```xml
   <!-- eurekaclient -->
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   <!-- 配置中心 -->
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-config</artifactId>
   </dependency>
   <!-- MyBatis -->
   <dependency>
     <groupId>org.mybatis.spring.boot</groupId>
     <artifactId>mybatis-spring-boot-starter</artifactId>
     <version>2.1.3</version>
   </dependency>
   <!-- MySQL 驱动 -->
   <dependency>
     <groupId>mysql</groupId>
     <artifactId>mysql-connector-java</artifactId>
     <version>8.0.19</version>
   </dependency>
   ```

2. 在 resources 目录下创建 bootstrap.yml，在该文件中配置拉取 Git 仓库相关配置文件的信息

   ```yaml
   spring:
     cloud:
       config:
         name: order # 对应的配置文件名称
         label: master # Git仓库分支名
         discovery:
           enabled: true
           serviceId: configserver # 连接的配置中心名称
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8761/eureka/
   ```

   在 Git 仓库配置文件 order.yml 中添加配置信息，服务提供者 order 集成 MyBatis 环境

   ```yaml
   server:
     port: 8040
   spring:
     application:
       name: order
     datasource:
       name: orderingsystem
       url: jdbc:mysql://localhost:3306/orderingsystem?useUnicode=true&characterEncoding=UTF-8
       username: root
       password: 123456
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/
     instance:
       prefer-ip-address: true
   mybatis:
     mapper-locations: classpath:mapping/*.xml
     type-aliases-package: com.southwind.entity
   ```

3. 创建entity包，新建 Order 类，对应数据表 t_order

   ```java
   @Data
   public class Order {
     private long id;
     private User user;
     private Menu menu;
     private Admin admin;
     private Date date;
     private int state;
   }
   ```

   新建 OrderVO 类为 layui 框架提供封装类

   ```java
   @Data
   public class OrderVO {
     private int code;
     private String msg;
     private int count;
     private List<Order> data;
   }
   ```

4. 创建 repository 包，新建 OrderRepository 接口

   ```java
   public interface OrderRepository {
     void save(Order order);
     List<Order> findAllByUid(long uid,int index,int limit);
     int countByUid(long uid);
     void deleteByMid(long mid);
     void deleteByUid(long uid);
     List<Order> findAllByState(int state,int index,int limit);
     int countByState(int state);
     void updateState(long id,long aid,int state);
   }
   ```

5. 在 resources 目录下创建 mapping 文件夹，存放 Mapper.xml

   新建 OrderRepository.xml，编写 OrderRepository 接口方法对应的 SQL

   ```xml
   <mapper namespace="com.southwind.repository.OrderRepository">
     <resultMap id="orderMap" type="Order">
    <id property="id" column="oid"/>
       <result property="date" column="date"/>
       <result property="state" column="state"/>
       <!-- 映射 menu -->
       <association property="menu" javaType="Menu">
         <id property="id" column="mid"/>
         <result property="name" column="name"/>
         <result property="price" column="price"/>
         <result property="flavor" column="flavor"/>
       </association>
     </resultMap>
     <resultMap id="orderMap2" type="Order">
       <id property="id" column="oid"/>
       <result property="date" column="date"/>
       <!-- 映射 menu -->
       <association property="menu" javaType="Menu">
         <id property="id" column="mid"/>
         <result property="name" column="name"/>
         <result property="price" column="price"/>
         <result property="flavor" column="flavor"/>
       </association>
       <!-- 映射 user -->
       <association property="user" javaType="User">
         <id property="id" column="uid"/>
         <result property="nickname" column="nickname"/>
         <result property="telephone" column="telephone"/>
         <result property="address" column="address"/>
       </association>
     </resultMap>
   
     <insert id="save" parameterType="Order">
       insert into t_order(uid,mid,aid,date,state) values(#{user.id},#{menu.id},#{admin.id},#{date},0)
     </insert>
     <select id="findAllByUid" resultMap="orderMap">
       select m.id mid,m.name,m.price,m.flavor,o.id oid,o.date,o.state from t_order o,t_menu m where o.mid = m.id and o.uid = #{param1} order by oid limit #{param2},#{param3}
     </select>
     <select id="countByUid" parameterType="long" resultType="int">
       select count(*) from t_order where uid = #{uid}
     </select>
     <delete id="deleteByMid" parameterType="long">
       delete from t_order where mid = #{mid}
     </delete>
     <delete id="deleteByUid" parameterType="long">
       delete from t_order where uid = #{uid}
     </delete>
     <select id="findAllByState" resultMap="orderMap2">
       select m.id mid,m.name,m.price,m.flavor,o.id oid,o.date,u.id uid,u.nickname,u.telephone,u.address from t_order o,t_menu m,t_user u where o.mid = m.id and o.uid = u.id and o.state = #{param1} order by oid limit #{param2},#{param3}
     </select>
     <select id="countByState" parameterType="int" resultType="int">
       select count(*) from t_order where state = #{state}
     </select>
     <update id="updateState">
       update t_order set aid = #{param2},state = #{param3} where id = #{param1}
     </update>
   </mapper>
   ```
   
   将 Mapper 注入，在启动类添加注解 `@MapperScan("com.southwind.repository")`
   
6. 新建 OrderHandler，将 OrderRepository 通过 @Autowired 注解进行注入，完成相关业务逻辑

   ```java
   @RestController
   @RequestMapping("/order")
public class OrderHandler {
     @Autowired private OrderRepository orderRepository;
   
     @PostMapping("/save")
     public void save(@RequestBody Order order){
       orderRepository.save(order);
     }
     @GetMapping("/findAllByUid/{uid}/{page}/{limit}")
     public OrderVO findAllByUid(@PathVariable("uid") long uid, @PathVariable("page") int page, @PathVariable("limit") int limit){
       OrderVO orderVO = new OrderVO();
       orderVO.setCode(0);
       orderVO.setMsg("");
       orderVO.setCount(orderRepository.countByUid(uid));
       orderVO.setData(orderRepository.findAllByUid(uid,(page-1)*limit,limit));
       return orderVO;
     }
     @DeleteMapping("/deleteByMid/{mid}")
     public void deleteByMid(@PathVariable("mid") long mid){
       orderRepository.deleteByMid(mid);
     }
     @DeleteMapping("/deleteByUid/{uid}")
     public void deleteByUid(@PathVariable("uid") long uid){
       orderRepository.deleteByUid(uid);
     }
     @GetMapping("/findAllByState/{state}/{page}/{limit}")
     public OrderVO findAllByState(@PathVariable("state") int state, @PathVariable("page") int page, @PathVariable("limit") int limit){
       OrderVO orderVO = new OrderVO();
       orderVO.setCode(0);
       orderVO.setMsg("");
       orderVO.setCount(orderRepository.countByState(0));
       orderVO.setData(orderRepository.findAllByState(0,(page-1)*limit,limit));
       return orderVO;
     }
     @PutMapping("/updateState/{id}/{state}/{aid}")
     public void updateState(@PathVariable("id") long id, @PathVariable("state") int state, @PathVariable("aid") long aid){
       orderRepository.updateState(id,aid,state);
     }
   }
   ```
   
7. 依次启动注册中心、configserver、OrderApplication。调用接口

   ```shell
   curl -X POST http://localhost:8040/order/save -d '{"user":{"id":"1"},"menu":{"id":"11"},"admin":{"id":"1"},"date":"2020-07-17"}' -H "Content-Type: application/json"
   curl -X GET http://localhost:8040/order/findAllByUid/1/1/100
   curl -X DELETE http://localhost:8040/order/deleteByMid/mid
   curl -X DELETE http://localhost:8040/order/deleteByUid/uid
   curl -X GET http://localhost:8040/order/findAllByState/0/1/100
   curl -X PUT http://localhost:8040/order/updateState/28/1/1
   ```

### 总结
本节课我们讲解了项目实战 order 模块的搭建，作为一个服务提供者，order 为整个系统提供订单服务，包括添加订单、查询订单、删除订单、处理订单。