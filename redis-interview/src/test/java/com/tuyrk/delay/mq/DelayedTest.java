package com.tuyrk.delay.mq;

import com.tuyrk.RedisInterviewApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

public class DelayedTest extends RedisInterviewApplicationTests {
    @Autowired
    private DelayedSender sender;

    @Test
    public void test() throws InterruptedException {
        sender.send("Hi Admin.");
        TimeUnit.SECONDS.sleep(5); // 等待接收程序执行之后，再退出测试
    }
}
