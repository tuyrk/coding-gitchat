package com.learning.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;


@Component
@Aspect
public class CarAop {
    @Pointcut("execution(* com.learning.aop.Person.drive1())")
    public void b() {
    }

    @Before("b()")
    public void before() {
        System.out.println("巡视车体及周围情况");
        System.out.println("发动");
    }

    @After("execution(* com.learning.aop.Person.drive1())")
    public void after() {
        System.out.println("熄火");
        System.out.println("锁车");
    }

    @Around("execution(* com.learning.aop.Person.drive2(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("==============");
        // 获取方法参数值数组
        Object[] args = pjp.getArgs();
        // 获取签名
        Signature signature = pjp.getSignature();
        // 获取方法签名
        MethodSignature methodSignature = (MethodSignature) signature;
        // 获取方法参数类型数组
        Class[] parameterTypes = methodSignature.getParameterTypes();

        System.out.println();
        // Object proceed = pjp.proceed();
        // System.out.println(proceed);
        pjp.proceed(new String[]{"tuyk"});

        return pjp;
    }
}
