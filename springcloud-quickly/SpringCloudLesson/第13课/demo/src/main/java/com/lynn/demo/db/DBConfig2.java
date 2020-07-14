package com.lynn.demo.db;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mysql.datasource.test2")
@SpringBootConfiguration
public class DBConfig2 extends DBConfig {
}
