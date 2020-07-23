package com.interview;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * JDK 原生动态代理是怎么实现的 + 面试题
 */
public class Lesson4_2 {
    // 调用静态方法
    @Test
    public void staticMd() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class myClass = Class.forName(MyReflect.class.getName());
        System.out.println(myClass.getCanonicalName());

        Method method = myClass.getMethod("staticMd");
        method.invoke(myClass);
    }

    // 调用公共方法
    @Test
    public void publicMd() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Class myClass = Class.forName(MyReflect.class.getName());

        Method method2 = myClass.getMethod("publicMd");
        method2.invoke(myClass.newInstance());
    }

    // 调用私有方法
    @Test
    public void privateMd() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Class myClass = Class.forName(MyReflect.class.getName());
        System.out.println(Arrays.stream(myClass.getMethods()).map(Method::getName).collect(Collectors.joining(" ")));
        System.out.println(Arrays.stream(myClass.getDeclaredMethods()).map(Method::getName).collect(Collectors.joining(" ")));

        Method method3 = myClass.getDeclaredMethod("privateMd");
        method3.setAccessible(true);
        method3.invoke(myClass.newInstance());
    }

    static class MyReflect {
        // 静态方法
        public static void staticMd() {
            System.out.println("Static Method");
        }

        // 公共方法
        public void publicMd() {
            System.out.println("Public Method");
        }

        // 私有方法
        private void privateMd() {
            System.out.println("Private Method");
        }
    }
}

// JDK 动态代理调用
class AnimalProxy implements InvocationHandler {
    private Animal target; // 代理对象

    public Animal getInstance(Class<? extends Animal> clazz) throws IllegalAccessException, InstantiationException {
        this.target = clazz.newInstance();
        // 取得代理对象
        System.out.println(Arrays.stream(clazz.getInterfaces()).map(Class::getName).collect(Collectors.joining(" ")));
        return (Animal) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("调用前");
        Object result = method.invoke(target, args); // 方法调用
        System.out.println("调用后");
        return result;
    }

    interface Animal {
        void eat();
    }

    static final class Dog implements Animal {
        @Override
        public void eat() {
            System.out.println("The dog is eating");
        }
    }

    static class Cat implements Animal {
        @Override
        public void eat() {
            System.out.println("The cat is eating");
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        AnimalProxy proxy = new AnimalProxy();
        Animal dogProxy = proxy.getInstance(Dog.class);
        dogProxy.eat();
    }
}

// cglib 动态代理调用
class CglibProxy implements MethodInterceptor {
    static class Panda {
        public void eat() {
            System.out.println("The panda is eating");
        }
    }

    public Object getInstance(Class<Panda> clazz) {
        // 创建代理对象(设置父类为实例类, 回调方法)
        return Enhancer.create(clazz, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("调用前");
        Object result = methodProxy.invokeSuper(o, objects); // 执行方法调用
        System.out.println("调用后");
        return result;
    }

    public static void main(String[] args) {
        CglibProxy proxy = new CglibProxy();
        Panda panda = (Panda) proxy.getInstance(Panda.class);
        panda.eat();
    }
}

