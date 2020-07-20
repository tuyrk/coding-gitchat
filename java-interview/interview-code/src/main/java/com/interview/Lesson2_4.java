package com.interview;

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

/**
 * 克隆和序列化应用 + 面试题
 */
public class Lesson2_4 {
    // 克隆的问题
    @Test
    public void cloneProblem() {
        // 等号赋值（ 基本类型）
        int number1 = 6;
        int number2 = number1;
        // 修改 number2 的值
        number2 = 9;
        System.out.println("number1：" + number1);
        System.out.println("number2：" + number2);

        Dog dog = new Dog("旺财", 5, null);
        Dog dog2 = dog;
        // 修改 dog2 的值
        dog2.setName("大黄");
        dog2.setAge(3);
        System.out.println(dog);
        System.out.println(dog2);
    }

    // 克隆
    @Test
    public void testClone() throws CloneNotSupportedException {
        Dog dog = new Dog("旺财", 5, null);
        // -------------------------- 克隆 ----------------------------
        Dog dog3 = (Dog) dog.clone();
        System.out.println(dog);
        System.out.println(dog3);
    }

    // 浅克隆
    @Test
    public void shallowClone() throws CloneNotSupportedException {
        Dog dog4 = new Dog("大黄", 0, new DogChild("二狗"));

        Dog dog5 = (Dog) dog4.clone();
        dog5.setName("旺财");
        dog5.getDogChild().setName("狗二");

        System.out.println("dog name 4：" + dog4.getName()); // 大黄
        System.out.println("dog name 5：" + dog5.getName()); // 旺财
        System.out.println("dog child name 4：" + dog4.getDogChild().getName()); // 狗二
        System.out.println("dog child name 5：" + dog5.getDogChild().getName()); // 狗二
    }


    // 深克隆（序列化）
    @Test
    public void deepCloneSerial() {
        Bird bird1 = new Bird("小鸟", new BirdChild("小小鸟"));
        Bird bird2 = CloneUtils.clone(bird1);
        bird2.setName("黄雀");
        bird2.getBirdChild().setName("小黄雀");

        System.out.println("bird name 1:" + bird1);
        System.out.println("bird name 2:" + bird2);
    }

    // 深克隆（引用类型实现克隆）
    @Test
    public void deepCloneQuote() throws CloneNotSupportedException {
        Parrot parrot1 = new Parrot("大鹦鹉", new ParrotChild("小鹦鹉"));
        // 克隆
        Parrot parrot2 = (Parrot) parrot1.clone();
        parrot2.setName("老鹦鹉");
        parrot2.getParrotChild().setName("少鹦鹉");

        System.out.println("parrot name 1:" + parrot1);
        System.out.println("parrot name 2:" + parrot2);
    }

    @Test
    public void deepClone() throws CloneNotSupportedException {
        CloneObj cloneObj1 = new CloneObj("老王", 30, new int[]{18, 19});

        CloneObj cloneObj2 = (CloneObj) cloneObj1.clone();
        cloneObj2.setName("磊哥");
        cloneObj2.setAge(33);
        cloneObj2.getSistersAge()[0] = 20;

        System.out.println(cloneObj1);
        System.out.println(cloneObj2);
    }
}

// 序列化和反序列化
class SerializableTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // 对象赋值
        User user = new User("老王", 30);
        System.out.println(user);

        // -------------------------- Java 原生序列化 ----------------------------
        // 创建输出流（序列化内容到磁盘）
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("test.out"));
        // 序列化对象
        oos.writeObject(user);
        oos.flush();
        oos.close();

        // 创建输入流（从磁盘反序列化）
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("test.out"));
        // 反序列化
        User user2 = (User) ois.readObject();
        ois.close();
        System.out.println(user2);

        // -------------------------- JSON 序列化（fastjson） ----------------------------
        String jsonSerialize = JSON.toJSONString(user);
        User user3 = JSON.parseObject(jsonSerialize, User.class);
        System.out.println(user3);

        // --------------------------  Hessian 序列化 ----------------------------
        // 序列化
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        HessianOutput hessianOutput = new HessianOutput(bo);
        hessianOutput.writeObject(user);
        byte[] hessianBytes = bo.toByteArray();
        // 反序列化
        ByteArrayInputStream bi = new ByteArrayInputStream(hessianBytes);
        HessianInput hessianInput = new HessianInput(bi);
        User user4 = (User) hessianInput.readObject();
        System.out.println(user4);
    }
}

@Data
@AllArgsConstructor
class Dog implements Cloneable {
    private String name;
    private int age;
    private DogChild dogChild;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

@Data
@AllArgsConstructor
class DogChild {
    private String name;
}

@Data
@AllArgsConstructor
class Bird implements Serializable {
    private static final long serialVersionUID = -8548202819636883145L;
    private String name;
    private BirdChild birdChild;
}

@Data
@AllArgsConstructor
class BirdChild implements Serializable {
    private static final long serialVersionUID = -2767034691908202986L;
    private String name;
}

@Data
@AllArgsConstructor
class Parrot implements Cloneable {
    private String name;
    private ParrotChild parrotChild;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Parrot bird = (Parrot) super.clone();
        bird.parrotChild = (ParrotChild) parrotChild.clone();
        return bird;
    }
}

@Data
@AllArgsConstructor
class ParrotChild implements Cloneable {
    private String name;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

/**
 * 克隆工具类（通过序列化方式实现深拷贝）
 * 先将要拷贝对象写入到内存中的字节流中，然后再从这个字节流中读出刚刚存储的信息，作为一个新对象返回，那么这个新对象和原对象就不存在任何地址上的共享，自然实现了深拷贝。
 **/
class CloneUtils {
    public static <T extends Serializable> T clone(T obj) {
        T cloneObj = null;
        try {
            //写入字节流
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bo);
            oos.writeObject(obj);
            oos.close();

            //分配内存,写入原始对象,生成新对象
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());//获取上面的输出字节流
            ObjectInputStream oi = new ObjectInputStream(bi);

            //返回生成的新对象
            cloneObj = (T) oi.readObject();
            oi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloneObj;
    }
}

@Data
@AllArgsConstructor
class CloneObj implements Cloneable {
    private String name;
    private int age;
    private int[] sistersAge;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

@Data
@AllArgsConstructor
class User implements Serializable {
    private static final long serialVersionUID = 5132320539584511249L;
    private String name;
    private int age;
}
