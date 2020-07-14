# 服务提供者 menu

title: 19-服务提供者 menu
date: 2020-07-14 10:11:46
categories: [GitChat,SpringCloud极简入门]
tags: [springcloud]

---

@[TOC]

本节课我们来实现服务提供者 menu。menu 为系统提供菜品相关服务，包括添加菜品、查询菜品、修改菜品、删除菜品，具体实现如下所示。

1. 在父工程下创建建名为 menu 的 Module，pom.xml 添加相关依赖

   menu 配置文件从 Git 仓库拉取，所以需要添加 Spring Cloud Config 相关依赖；同时需要访问数据库，因此还要添加 MyBatis 相关依赖。

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
         name: menu # 对应的配置文件名称
         label: master # Git仓库分支名
         discovery:
           enabled: true
           serviceId: configserver # 连接的配置中心名称
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8761/eureka/
   ```

   在 Git 仓库配置文件 menu.yml 中添加配置信息，服务提供者 menu 集成 MyBatis 环境

   ```yaml
   server:
     port: 8020
   spring:
     application:
       name: menu
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

3. 创建entity包，新建 Menu 类，对应数据表 t_menu

   ```java
   @Data
   public class Menu {
     private long id;
     private String name;
     private double price;
     private String flavor;
     private Type type;
   }
   ```

   新建 MenuVO 类为 layui 框架提供封装类

   ```java
   @Data
   public class MenuVO {
     private int code;
     private String msg;
     private int count;
     private List<Menu> data;
   }
   ```

   新建 Type 类，对应数据表 t_type

   ```java
   @Data
   public class Type {
     private long id;
     private String name;
   }
   ```

4. 创建 repository 包，新建 MenuRepository 接口

   ```java
   public interface MenuRepository {
     List<Menu> findAll(int index,int limit);
     int count();
     void save(Menu menu);
     Menu findById(long id);
     void update(Menu menu);
     void deleteById(long id);
   }
   ```

   新建 TypeRepository 接口

   ```java
   public interface TypeRepository {
     List<Type> findAll();
   }
   ```

5. 在 resources 目录下创建 mapping 文件夹，存放 Mapper.xml

   新建 MenuRepository.xml，编写 MenuRepository 接口方法对应的 SQL
   
   ```xml
   <mapper namespace="com.southwind.repository.MenuRepository">
     <resultMap id="menuMap" type="Menu">
       <id property="id" column="mid"/>
       <result property="name" column="mname"/>
       <result property="author" column="author"/>
       <result property="price" column="price"/>
       <result property="flavor" column="flavor"/>
       <!-- 映射 type -->
       <association property="type" javaType="Type">
         <id property="id" column="tid"/>
         <result property="name" column="tname"/>
       </association>
     </resultMap>
   
     <select id="findAll" resultMap="menuMap">
       select m.id mid,m.name mname,m.price,m.flavor,t.id tid,t.name tname from t_menu m,t_type t where m.tid = t.id order by mid limit #{param1},#{param2}
     </select>
     <select id="count" resultType="int">
       select count(*) from t_menu
     </select>
     <insert id="save" parameterType="Menu">
       insert into t_menu(name,price,flavor,tid) values(#{name},#{price},#{flavor},#{type.id})
     </insert>
     <select id="findById" resultMap="menuMap">
       select id mid,name mname,price,flavor,tid from t_menu where id = #{id}
     </select>
     <update id="update" parameterType="Menu">
       update t_menu set name = #{name},price = #{price},flavor = #{flavor},tid = #{type.id} where id = #{id}
     </update>
     <delete id="deleteById" parameterType="long">
       delete from t_menu where id = #{id}
     </delete>
   </mapper>
   ```
   
   新建 TypeRepository.xml，编写 TypeRepository 接口方法对应的 SQL
   
   ```xml
   <mapper namespace="com.southwind.repository.TypeRepository">
     <select id="findAll" resultType="Type">
       select * from t_type
     </select>
   </mapper>
   ```
   
   将 Mapper 注入，在启动类添加注解 `@MapperScan("com.southwind.repository")`
   
6. 新建 MenuHandler，将 MenuRepository 通过 @Autowired 注解进行注入，完成相关业务逻辑

   ```java
   @RestController
   @RequestMapping("/menu")
   public class MenuHandler {
     @Autowired private MenuRepository menuRepository;
     @Autowired private TypeRepository typeRepository;
   
     @GetMapping("/findAll/{page}/{limit}")
     public MenuVO findAll(@PathVariable("page") int page, @PathVariable("limit") int limit){
       MenuVO menuVO = new MenuVO();
       menuVO.setCode(0);
       menuVO.setMsg("");
       menuVO.setCount(menuRepository.count());
       menuVO.setData(menuRepository.findAll((page-1)*limit,limit));
       return menuVO;
     }
     @GetMapping("/findAll")
     public List<Type> findAll(){
       return typeRepository.findAll();
     }
     @PostMapping("/save")
     public void save(@RequestBody Menu menu){
       menuRepository.save(menu);
     }
     @GetMapping("/findById/{id}")
     public Menu findById(@PathVariable("id") long id){
       return menuRepository.findById(id);
     }
     @PutMapping("/update")
     public void update(@RequestBody Menu menu){
       menuRepository.update(menu);
     }
     @DeleteMapping("/deleteById/{id}")
     public void deleteById(@PathVariable("id") long id){
       menuRepository.deleteById(id);
     }
   }
   ```

7. 依次启动注册中心、configserver、MenuApplication。调用接口

   ```shell
   curl -X GET http://localhost:8020/menu/findAll/2/5
   curl -X GET http://localhost:8020/menu/findAll
   curl -X POST http://localhost:8020/menu/save -d '{"name":"酸汤肥牛","price":"4.5","flavor":"五香","type":{"id":4}}' -H "Content-Type: application/json"
   curl -X GET http://localhost:8020/menu/findById/28
   curl -X PUT http://localhost:8020/menu/update -d '{"id":28,"name":"酸汤肥牛","price":"4.5","flavor":"五香","type":{"id":1}}' -H "Content-Type: application/json"
   curl -X DELETE http://localhost:8020/menu/deleteById/28
   ```

### 总结
本节课我们讲解了项目实战 menu 模块的搭建，作为一个服务提供者，menu 为整个系统提供菜品服务，包括添加菜品、查询菜品、修改菜品、删除菜品。

