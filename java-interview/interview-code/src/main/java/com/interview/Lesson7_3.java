package com.interview;

import org.junit.Test;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Lesson7_3 {
    // 单例模式
    @Test
    public void singleton() {
        Singleton singleton1 = Singleton.getInstance();
        Singleton singleton2 = Singleton.getInstance();
        System.out.println(singleton1 == singleton2);   // output:true
    }

    // 简单工厂
    @Test
    public void factory() {
        String mocca = Factory.createProduct("Mocca");
        System.out.println("mocca = " + mocca);
    }

    // 抽象工厂
    @Test
    public void absFactory() {
        AbstractFactory coffeeFactory = new CoffeeFactory();
        String result = coffeeFactory.createProduct("Latte");
        System.out.println(result); // output:拿铁
    }

    // 观察者模式
    @Test
    public void observer() {
        // 定义发布者
        ConcreteSubject concreteSubject = new ConcreteSubject();
        // 定义订阅者
        ConcrereObserver concrereObserver = new ConcrereObserver("老王");
        ConcrereObserver concrereObserver2 = new ConcrereObserver("Java");
        // 添加订阅
        concreteSubject.attach(concrereObserver);
        concreteSubject.attach(concrereObserver2);
        // 发布信息
        concreteSubject.notify("更新了");
    }

    // 装饰器模式
    @Test
    public void decorator() {
        LaoWang laoWang = new LaoWang();
        Jacket jacket = new Jacket(laoWang);
        Hat hat = new Hat(jacket);
        hat.show();
    }

    // 模板方法模式
    @Test
    public void template() {
        Refrigerator refrigerator = new Banana();
        refrigerator.open();
        refrigerator.put();
        refrigerator.close();
        refrigerator.action();
    }

    // 代理模式
    @Test
    public void proxy() {
        IAirTicket airTicket = new ProxyAirTicket();
        airTicket.buy();
    }

    // 策略模式
    @Test
    public void strategy() {
        Trip trip = new Trip(new Bike());
        trip.doTrip();
    }

    // 适配器模式
    @Test
    public void adapter() {
        TypeC typeC = new TypeC();
        MicroUSB microUSB = new AdapterMicroUSB(typeC);
        microUSB.charger();
    }
}

//<editor-fold desc="单例模式">

// 单例模式
class Singleton {
    private static Singleton instance = new Singleton();

    public static Singleton getInstance() {
        return instance;
    }
}

// 单例模式-延迟加载
class SingletonLazy {
    private static SingletonLazy instance;

    public static synchronized SingletonLazy getInstance() {
        if (instance == null) {
            instance = new SingletonLazy();
        }
        return instance;
    }
}
//</editor-fold>

//<editor-fold desc="工厂模式">

// 简单工厂
class Factory {
    public static String createProduct(String product) {
        String result = null;
        switch (product) {
            case "Mocca":
                result = "摩卡";
                break;
            case "Latte":
                result = "拿铁";
                break;
            default:
                result = "其他";
                break;
        }
        return result;
    }
}

// 抽象工厂
abstract class AbstractFactory {
    public abstract String createProduct(String product);
}

// 啤酒工厂
class BeerFactory extends AbstractFactory {
    @Override
    public String createProduct(String product) {
        String result = null;
        switch (product) {
            case "Hans":
                result = "汉斯";
                break;
            case "Yanjing":
                result = "燕京";
                break;
            default:
                result = "其他啤酒";
                break;
        }
        return result;
    }
}

// 咖啡工厂
class CoffeeFactory extends AbstractFactory {
    @Override
    public String createProduct(String product) {
        String result = null;
        switch (product) {
            case "Mocca":
                result = "摩卡";
                break;
            case "Latte":
                result = "拿铁";
                break;
            default:
                result = "其他咖啡";
                break;
        }
        return result;
    }
}
//</editor-fold>

//<editor-fold desc="观察者模式">

// 观察者（消息接收方）
interface Observer {
    public void update(String message);
}

// 具体的观察者（消息接收方）
class ConcrereObserver implements Observer {
    private String name;

    public ConcrereObserver(String name) {
        this.name = name;
    }

    @Override
    public void update(String message) {
        System.out.println(name + "：" + message);
    }
}

// 被观察者（消息发布方）
interface Subject {
    // 增加订阅者
    public void attach(Observer observer);

    // 删除订阅者
    public void detach(Observer observer);

    // 通知订阅者更新消息
    public void notify(String message);
}

// 具体被观察者（消息发布方）
class ConcreteSubject implements Subject {
    // 订阅者列表（存储信息）
    private List<Observer> list = new ArrayList<Observer>();

    @Override
    public void attach(Observer observer) {
        list.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        list.remove(observer);
    }

    @Override
    public void notify(String message) {
        for (Observer observer : list) {
            observer.update(message);
        }
    }
}
//</editor-fold>

//<editor-fold desc="装饰器模式">

// 顶级对象
interface IPerson {
    void show();
}

// 装饰器超类
class DecoratorBase implements IPerson {
    IPerson iPerson;

    public DecoratorBase(IPerson iPerson) {
        this.iPerson = iPerson;
    }

    @Override
    public void show() {
        iPerson.show();
    }
}

// 具体装饰器
class Jacket extends DecoratorBase {
    public Jacket(IPerson iPerson) {
        super(iPerson);
    }

    @Override
    public void show() {
        iPerson.show(); // 执行已有功能
        System.out.println("穿上夹克");
    }
}

class Hat extends DecoratorBase {
    public Hat(IPerson iPerson) {
        super(iPerson);
    }

    @Override
    public void show() {
        iPerson.show(); // 执行已有功能
        System.out.println("戴上帽子");
    }
}

// 具体对象
class LaoWang implements IPerson {
    @Override
    public void show() {
        System.out.println("什么都没穿");
    }
}
//</editor-fold>

//<editor-fold desc="模板方法模式">

abstract class Refrigerator {
    public void open() {
        System.out.println("开冰箱门");
    }

    protected abstract void put();

    public void close() {
        System.out.println("关冰箱门");
    }

    public void action() {
        System.out.println("开冰箱门");
        put();
        System.out.println("关冰箱门");
    }
}

class Banana extends Refrigerator {
    @Override
    public void put() {
        System.out.println("放香蕉");
    }
}

class Apple extends Refrigerator {
    @Override
    public void put() {
        System.out.println("放苹果");
    }
}
//</editor-fold>

//<editor-fold desc="代理模式">

// 定义售票接口
interface IAirTicket {
    void buy();
}

// 定义飞机场售票
class AirTicket implements IAirTicket {
    @Override
    public void buy() {
        System.out.println("买票");
    }
}

// 代理售票平台
class ProxyAirTicket implements IAirTicket {

    private AirTicket airTicket;

    public ProxyAirTicket() {
        airTicket = new AirTicket();
    }

    @Override
    public void buy() {
        airTicket.buy();
    }
}
//</editor-fold>

//<editor-fold desc="策略模式">

// 声明旅行
interface ITrip {
    void going();
}

class Bike implements ITrip {
    @Override
    public void going() {
        System.out.println("骑自行车");
    }
}

class Drive implements ITrip {
    @Override
    public void going() {
        System.out.println("开车");
    }
}

// 出行类
class Trip {
    private ITrip trip;

    public Trip(ITrip trip) {
        this.trip = trip;
    }

    public void doTrip() {
        this.trip.going();
    }
}
//</editor-fold>

//<editor-fold desc="适配器模式">

// 传统的充电线 MicroUSB
interface MicroUSB {
    void charger();
}

// TypeC 充电口
interface ITypeC {
    void charger();
}

class TypeC implements ITypeC {
    @Override
    public void charger() {
        System.out.println("TypeC 充电");
    }
}

// 适配器
class AdapterMicroUSB implements MicroUSB {
    private TypeC typeC;

    public AdapterMicroUSB(TypeC typeC) {
        this.typeC = typeC;
    }

    @Override
    public void charger() {
        typeC.charger();
    }
}
//</editor-fold>
