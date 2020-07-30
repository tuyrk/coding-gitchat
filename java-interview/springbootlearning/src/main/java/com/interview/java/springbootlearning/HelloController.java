package com.interview.java.springbootlearning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${app.name}")
    private String appName;

    @Autowired
    private Environment environment;

    @Autowired
    private AppConfiguration appConfiguration;


    @RequestMapping("/index")
    public String index(String hiName) {
        System.out.println(appName);
        System.out.println(environment.getProperty("app.name"));
        System.out.println(appConfiguration.getName());
        return "Hello, " + hiName + " |@" + appName;
    }
}
