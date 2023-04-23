package com.bsi.md.agent.pulsar;

import org.apache.pulsar.client.api.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PulsarTest {
    public static void main(String[] args) {
        String url = "pulsar://139.9.27.37:6650";
        PulsarClientSimulator client = new PulsarClientSimulator(url,
                false, false, false,
                PulsarConst.AUTH_TYPE_NONE, "", "", "", "", "");

        client.getPulsarClient();

        String message = "Hello, world!"; // 定义一个字符串
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // 生产消息的线程
        executor.submit(() -> {
            try {
                PulsarProducerSimulator producer = new PulsarProducerSimulator("my-topic",client);

                for (int i = 0; i < 10; i++) {
                    String msg = message + " " + i;
                    producer.produce(msg,i+"");
                    System.out.printf("Produced message: %s%n", msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 消费消息的线程
        executor.submit(() -> {
            try {
                PulsarConsumerSimulator consumer = new PulsarConsumerSimulator("my-topic",client);
                while (true) {
                    Message<byte[]> msg = consumer.receive();
                    System.out.printf("Consumed message: %s%n", msg==null?"null":new String(msg.getValue()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
