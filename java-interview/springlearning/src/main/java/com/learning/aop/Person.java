package com.learning.aop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("person")
public class Person {
    @Value("${jdbc.username}")
    private String userName;

    public void drive1() {
        System.out.println("开车");
    }

    public void drive2(String name) {
        System.out.println(userName);
        System.out.println(name + "开车");
    }
}
