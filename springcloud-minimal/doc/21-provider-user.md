# 服务提供者 user

本节课我们来实现服务提供者 user，user 为系统提供用户相关服务，包括添加用户、查询用户、删除用户，具体实现如下所示。

1. 在父工程下创建建名为 user 的 Module，pom.xml 添加相关依赖

   user 配置文件从 Git 仓库拉取，所以需要添加 Spring Cloud Config 相关依赖；同时需要访问数据库，因此还要添加 MyBatis 相关依赖

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
         name: user # 对应的配置文件名称
         label: master # Git仓库分支名
         discovery:
           enabled: true
           serviceId: configserver # 连接的配置中心名称
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:8761/eureka/
   ```

   在 Git 仓库配置文件 order.yml 中添加配置信息，服务提供者 user 集成 MyBatis 环境

   ```yaml
   server:
     port: 8050
   spring:
     application:
       name: user
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

3. 创建entity包，新建 User 类，对应数据表 t_user

   ```java
   @Data
   public class User {
     private long id;
     private String username;
     private String password;
     private String nickname;
     private String gender;
     private String telephone;
     private Date registerdate;
     private String address;
   }
   ```

   新建 UserVO 类为 layui 框架提供封装类

   ```java
   @Data
   public class UserVO {
     private int code;
     private String msg;
     private int count;
     private List<User> data;
   }
   ```

4. 创建 repository 包，新建 UserRepository 接口

   ```java
   public interface UserRepository {
     List<User> findAll(int index, int limit);
     int count();
     void save(User user);
     void deleteById(long id);
   }
   ```

5. 在 resources 目录下创建 mapping 文件夹，存放 Mapper.xml

   新建 UserRepository.xml，编写 UserRepository 接口方法对应的 SQL
   
   ```xml
   <mapper namespace="com.southwind.repository.UserRepository">
     <select id="findAll" resultType="User">
       select * from t_user order by id limit #{param1},#{param2}
     </select>
     <select id="count" resultType="int">
       select count(*) from t_user
     </select>
     <insert id="save" parameterType="User">
       insert into t_user(username,password,nickname,gender,telephone,registerdate,address) values(#{username},#{password},#{nickname},#{gender},#{telephone},#{registerdate},#{address})
     </insert>
     <delete id="deleteById" parameterType="long">
       delete from t_user where id = #{id}
     </delete>
   </mapper>
   ```
   
   将 Mapper 注入，在启动类添加注解 `@MapperScan("com.southwind.repository")`
   
6. 新建 UserHandler，将 UserRepository 通过 @Autowired 注解进行注入，完成相关业务逻辑

   ```java
   @RestController
   @RequestMapping("/user")
   public class UserHandler {
     @Autowired private UserRepository userRepository;
   
     @GetMapping("/findAll/{page}/{limit}")
     public UserVO findAll(@PathVariable("page") int page, @PathVariable("limit") int limit){
       UserVO userVO = new UserVO();
       userVO.setCode(0);
       userVO.setMsg("");
       userVO.setCount(userRepository.count());
       userVO.setData(userRepository.findAll((page-1)*limit,limit));
       return userVO;
     }
     @PostMapping("/save")
     public void save(@RequestBody User user){
       user.setRegisterdate(new Date());
       userRepository.save(user);
     }
     @DeleteMapping("/deleteById/{id}")
     public void deleteById(@PathVariable("id") long id){
       userRepository.deleteById(id);
     }
   }
   ```

7. 依次启动注册中心、configserver、UserApplication。调用接口

   ```shell
   curl -X GET http://localhost:8050/user/findAll/1/100
   curl -X POST http://localhost:8050/user/save -d '{"username":"tuyrk","password":"123456","nickname":"神秘的小岛岛","gender":"男","telephone":"18382471393","address":"三色路"}' -H "Content-Type: application/json"
   curl -X DELETE http://localhost:8050/user/deleteById/6
   ```

### 总结
本节课我们讲解了项目实战 user 模块的搭建，作为一个服务提供者，user 为整个系统提供用户服务，包括添加用户、查询用户、删除用户。