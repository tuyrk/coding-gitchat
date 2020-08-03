package com.interview.other;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.remoting.exception.RemotingException;

import java.util.List;

public class RocketMQDemo {
    private static final String MQ_NAME_SRV_ADDR = "localhost:9876";

    public static void main(String[] args) {
        // 分组名
        String groupName = "myGroup-1";
        // 主题名
        String topicName = "myTopic-1";
        // 标签名
        String tagName = "myTag-1";

        new Thread(() -> {
            try {
                producer(groupName, topicName, tagName);
            } catch (InterruptedException | RemotingException | MQClientException | MQBrokerException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                consumer(groupName, topicName, tagName);
            } catch (MQClientException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 生产者
     *
     * @param groupName 分组名
     * @param topicName 主题名
     * @param tagName   标签名
     * @throws InterruptedException 异常
     * @throws RemotingException    异常
     * @throws MQClientException    异常
     * @throws MQBrokerException    异常
     */
    private static void producer(String groupName, String topicName, String tagName) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        DefaultMQProducer producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(MQ_NAME_SRV_ADDR);
        producer.start();
        String body = "Hello, 老王";
        Message message = new Message(topicName, tagName, body.getBytes());
        producer.send(message);
        producer.shutdown();
    }

    /**
     * 消费者
     *
     * @param groupName 分组名
     * @param topicName 主题名
     * @param tagName   标签名
     * @throws MQClientException 异常
     */
    private static void consumer(String groupName, String topicName, String tagName) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(MQ_NAME_SRV_ADDR);
        consumer.subscribe(topicName, tagName);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(
                    List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.println(new String(msg.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
}
