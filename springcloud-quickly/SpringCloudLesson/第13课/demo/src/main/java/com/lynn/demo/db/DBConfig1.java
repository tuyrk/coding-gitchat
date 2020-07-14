package com.lynn.demo.db;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mysql.datasource.test1")
@SpringBootConfiguration
public class DBConfig1 extends DBConfig {
}
