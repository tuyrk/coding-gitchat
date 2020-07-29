package com.learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyConfiguration {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 设置允许跨域的请求规则
                registry.addMapping("/api/**");
            }
        };
    }

    private TransactionTemplate transactionTemplate;

    public void add(Object user) throws Exception {
        // Spring编码式事务，回调机制
        transactionTemplate.execute(status -> {
            try {
            } catch (Exception e) {
                // 异常，设置为回滚
                status.setRollbackOnly();
                throw e;
            }
            return null;
        });
    }

    public void add2(Object user) throws Exception {
        // Spring编码式事务，回调机制
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                } catch (Exception e) {
                    // 异常，设置为回滚
                    status.setRollbackOnly();
                    throw e;
                }
            }
        });
    }
}
