package com.interview;

import com.interview.other.Config;
import com.interview.other.ConnectionFactoryUtil;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 消息队列面试题汇总(RabbitMQ)
 * https://github.com/vipstone/rabbitmq-java
 */
public class Lesson6_6 {
    public static void main(String[] args) throws IOException, TimeoutException {
        publisher();     // 生产消息
        consumer();     // 消费消息
    }

    /**
     * 推送消息
     */
    public static void publisher() throws IOException, TimeoutException {
        // 创建一个连接
        Connection conn = ConnectionFactoryUtil.GetRabbitConnection();
        if (conn != null) {
            try {
                // 创建通道
                Channel channel = conn.createChannel();
                // 声明队列【参数说明：参数一：队列名称，参数二：是否持久化；参数三：是否独占模式；参数四：消费者断开连接时是否删除队列；参数五：消息其他参数】
                channel.queueDeclare(Config.QueueName, false, false, false, null);
                String content = String.format("当前时间：%s", System.currentTimeMillis());
                // 发送内容【参数说明：参数一：交换机名称；参数二：队列名称，参数三：消息的其他属性-routing headers，此属性为MessageProperties.PERSISTENT_TEXT_PLAIN用于设置纯文本消息存储到硬盘；参数四：消息主体】
                channel.basicPublish("", Config.QueueName, null, content.getBytes(StandardCharsets.UTF_8));
                System.out.println("已发送消息：" + content);
                // 关闭连接
                channel.close();
                conn.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 消费消息
     */
    public static void consumer() throws IOException, TimeoutException {
        // 创建一个连接
        Connection conn = ConnectionFactoryUtil.GetRabbitConnection();
        if (conn != null) {
            try {
                // 创建通道
                Channel channel = conn.createChannel();
                // 声明队列【参数说明：参数一：队列名称，参数二：是否持久化；参数三：是否独占模式；参数四：消费者断开连接时是否删除队列；参数五：消息其他参数】
                channel.queueDeclare(Config.QueueName, false, false, false, null);

                // 创建订阅器，并接受消息
                channel.basicConsume(Config.QueueName, false, "", new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                               byte[] body) throws IOException {
                        String routingKey = envelope.getRoutingKey(); // 队列名称
                        String contentType = properties.getContentType(); // 内容类型
                        String content = new String(body, StandardCharsets.UTF_8); // 消息正文
                        System.out.println("消息正文：" + content);
                        channel.basicAck(envelope.getDeliveryTag(), false); // 手动确认消息【参数说明：参数一：该消息的index；参数二：是否批量应答，true批量确认小于index的消息】
                    }
                });

            } catch (Exception ignored) {
            }
        }
    }
}

