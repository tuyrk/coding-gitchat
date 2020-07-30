# 对数据库的基本操作步骤 + 面试题

1. MyBatis 有哪些优缺点？

   答：MyBatis 优缺点如下：

   优点：

   - 相比于 JDBC 需要编写的代码更少
   - 使用灵活，支持动态 SQL
   - 提供映射标签，支持对象与数据库的字段关系映射

   缺点：

   - SQL 语句依赖于数据库，数据库移植性差
   - SQL 语句编写工作量大，尤其在表、字段比较多的情况下

   总体来说，MyBatis 是一个非常不错的持久层解决方案，它专注于 SQL 本身，非常灵活，适用于需求变化较多的互联网项目，也是当前国内主流的 ORM 框架。

2. 以下不属于 MyBatis 优点的是？

   A：可以灵活的编辑 SQL 语句
   B：很好的支持不同数据库之间的迁移
   C：能够很好的和 Spring 框架集成
   D：提供映射标签支持对象和数据库的字段映射

   答：B

   题目解析：因为 MyBatis 需要自己编写 SQL 语句，但每个数据库的 SQL 语句有略有差异，所以 MyBatis 不能很好的支持不同数据库之间的迁移。

3. MyBatis 和 Hibernate 有哪些不同？

   答：MyBatis 和 Hibernate 都是非常优秀的 ORM 框架，它们的区别如下：

   - 灵活性：MyBatis 更加灵活，自己可以写 SQL 语句，使用起来比较方便；
   - 可移植性：MyBatis 有很多自己写的 SQL，因为每个数据库的 SQL 可以不相同，所以可移植性比较差；
   - 开发效率：Hibernate 对 SQL 语句做了封装，让开发者可以直接使用，因此开发效率更高；
   - 学习和使用门槛：MyBatis 入门比较简单，使用门槛也更低。

4. “#”和“$”有什么区别？

   答：“#”是预编译处理，“$”是字符替换。 在使用“#”时，MyBatis 会将 SQL 中的参数替换成“?”，配合 PreparedStatement 的 set 方法赋值，这样可以有效的防止 SQL 注入，保证程序的运行安全。

5. 在 MyBatis 中怎么解决实体类属性名和表字段名不一致的问题？

   答：通常的解决方案有以下两种方式。

   - 在 SQL 语句中重命名为实体类的属性名，可参考以下配置：

     ```xml
     <select id="selectorder" parametertype="int" resultetype="com.interview.order">
       select order_id id, order_no orderno form order where order_id=#{id};
     </select>
     ```

   - 通过 `<resultMap>` 映射对应关系，可参考以下配置：

     ```xml
     <resultMap id="BaseResultMap" type="com.interview.mybatislearning.model.UserEntity" >
       <id column="id" property="id" jdbcType="BIGINT" />
       <result column="username" property="userName" jdbcType="VARCHAR" />
       <result column="password" property="passWord" jdbcType="VARCHAR" />
       <result column="nick_name" property="nickName" jdbcType="VARCHAR" />
     </resultMap>
     <select id="getAll" resultMap="BaseResultMap">
       select * from t_user
     </select>
     ```

6. 在 MyBatis 中如何实现 like 查询？

     答：可以在 Java 代码中添加 SQL 通配符来实现 like 查询，这样也可以有效的防治 SQL 注入，具体实现如下：

     ```java
     String name = "%wang%":
     List<User> list = mapper.likeName(name);
     ```

     Mapper 配置：

     ```xml
     <select id="likeName">
       select * form t_user where name like #{name};
     </select>
     ```

7. MyBatis 有几种分页方式？

     答：MyBatis 的分页方式有以下两种：

     - 逻辑分页，使用 MyBatis 自带的 RowBounds 进行分页，它是一次性查询很多数据，然后在数据中再进行检索；
     - 物理分页，自己手写 SQL 分页或使用分页插件 PageHelper，去数据库查询指定条数的分页数据形式。

8. RowBounds 是一次性查询全部结果吗？为什么？

     答：RowBounds 表面是在“所有”数据中检索数据，其实**并非是一次性查询出所有数据**。因为 MyBatis 是对 JDBC 的封装，在 JDBC 驱动中有一个 Fetch Size 的配置，它规定了每次最多从数据库查询多少条数据，假如你要查询更多数据，它会在执行 next() 的时候，去查询更多的数据。 就好比你去自动取款机取 10000 元，但取款机每次最多能取 2500 元，要取 4 次才能把钱取完。对于 JDBC 来说也是一样，这样做的好处是可以有效的防止内存溢出。

9. 为什么阿里巴巴不允许使用 HashMap 或 Hashtable 作为查询结果集直接输出？

     答：因为使用 HashMap 或 Hashtable 作为查询结果集直接输出，会导致值类型不可控，给调用人员造成困扰，给系统带来更多不稳定的因素。

10. 什么是动态 SQL？

     答：动态 SQL 是指可以根据不同的参数信息来动态拼接的不确定的 SQL 叫做动态 SQL，MyBatis 动态 SQL 的主要元素有：if、choose/when/otherwise、trim、where、set、foreach 等。 以 if 标签的使用为例：

     ```xml
     <select id="findUser" parameterType="com.interview.entity.User" resultType="com.interview.entity.User">
       select * from t_user where
       <if test="id!=null">
         id = #{id}
       </if>
       <if test="username!=null">
         and username = #{username}
       </if>
       <if test="password!=null">
         and password = #{password}
       </if>
     </select>
     ```

11. 为什么不建议在程序中滥用事务？

      答：因为事务的滥用会影响数据的 QPS（每秒查询率），另外使用事务的地方还要考虑各方面回滚的方案，如缓存回滚、搜索引擎回滚、消息补偿、统计修正等。

12. 如何开启 MyBatis 的延迟加载？

      答：只需要在 mybatis-config.xml 设置 `<setting name="lazyLoadingEnabled" value="true"/>` 即可打开延迟缓存功能，完整配置文件如下：

      ```xml
      <configuration>
        <settings>
          <!-- 开启延迟加载 -->
          <setting name="lazyLoadingEnabled" value="true"/>
        </settings>
      </configuration>
      ```

13. 什么是 MyBatis 的一级缓存和二级缓存？

      答：MyBatis 缓存如下：

      - 一级缓存是 SqlSession 级别的，是 MyBatis 自带的缓存功能，并且无法关闭，因此当有两个 SqlSession 访问相同的 SQL 时，一级缓存也不会生效，需要查询两次数据库；
      - 二级缓存是 Mapper 级别的，只要是同一个 Mapper，无论使用多少个 SqlSession 来操作，数据都是共享的，多个不同的 SqlSession 可以共用二级缓存，MyBatis 二级缓存默认是关闭的，需要使用时可手动开启，二级缓存也可以使用第三方的缓存，比如，使用 Ehcache 作为二级缓存。

      手动开启二级缓存，配置如下：

      ```xml
      <configuration>
        <settings>
          <!-- 开启二级缓存 -->
          <setting name="cacheEnabled" value="true"/>
        </settings>
      </configuration>
      ```

14. 如何设置 Ehcache 为 MyBatis 的二级缓存？

      答：可直接在 XML 中配置开启 EhcacheCache，代码如下：

      ```xml
      <mapper namespace="com.interview.repository.ClassesReposirory"> 
        <!-- 开启二级缓存 -->
        <cache type="org.mybatis.caches.ehcache.EhcacheCache">
          <!-- 缓存创建以后，最后一次访问缓存的时间至失效的时间间隔 -->
          <property name="timeToIdleSeconds" value="3600"/>
          <!-- 缓存自创建时间起至失效的时间间隔-->
          <property name="timeToLiveSeconds" value="3600"/>
          <!-- 缓存回收策略，LRU 移除近期最少使用的对象 -->
          <property name="memoryStoreEvictionPolicy" value="LRU"/>
        </cache>
      
        <select id="findById" parameterType="java.lang.Long" resultType="com.interview.entity.Classes">
          select * from classes where id = #{id}
        </select>
      </mapper>
      ```

15. MyBatis 有哪些拦截器？如何实现拦截功能？

      答：MyBatis 提供的连接器有以下 4 种。

      - Executor：拦截内部执行器，它负责调用 StatementHandler 操作数据库，并把结果集通过 ResultSetHandler 进行自动映射，另外它还处理了二级缓存的操作
      - StatementHandler：拦截 SQL 语法构建的处理，它是 MyBatis 直接和数据库执行 SQL 脚本的对象，另外它也实现了 MyBatis 的一级缓存
      - ParameterHandler：拦截参数的处理
      - ResultSetHandler：拦截结果集的处理

      拦截功能具体实现如下：

      ```java
      @Intercepts({@Signature(type = Executor.class, method = "query",
                              args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
      public class TestInterceptor implements Interceptor {
        public Object intercept(Invocation invocation) throws Throwable {
          Object target = invocation.getTarget(); //被代理对象
          Method method = invocation.getMethod(); //代理方法
          Object[] args = invocation.getArgs(); //方法参数
          // 方法拦截前执行代码块
          Object result = invocation.proceed();
          // 方法拦截后执行代码块
          return result;
        }
        public Object plugin(Object target) {
          return Plugin.wrap(target, this);
        }
      }
      ```

      
