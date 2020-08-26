package com.tuyrk.delay.mq;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息消费者
 */
@Component
@RabbitListener(queues = DelayedConfig.QUEUE_NAME)
public class DelayedReceiver {
    @RabbitHandler
    public void process(String msg) {
        System.out.println("接收时间：" + System.currentTimeMillis());
        System.out.println("消息内容：" + msg);
    }
}
