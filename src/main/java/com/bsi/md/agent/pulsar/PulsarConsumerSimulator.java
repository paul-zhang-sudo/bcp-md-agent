package com.bsi.md.agent.pulsar;

import com.bsi.framework.core.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;
@Slf4j
public class PulsarConsumerSimulator {
    private final PulsarClientSimulator pulsarClientSimulator;

    private static final Integer MAX_RECEIVE_MSG = 100;

    private Consumer<byte[]> consumer;

    public PulsarConsumerSimulator(String topic,PulsarClientSimulator pulsarClientSimulator) {
        this.pulsarClientSimulator = pulsarClientSimulator;
        String msg = subscribe(topic);
    }

    private String subscribe(String topic) {
        try {
            if (consumer != null) {
                return "pulsar consumer already subscribe.";
            }
            PulsarClient pulsarClient = pulsarClientSimulator.getPulsarClient();
            consumer = pulsarClient.newConsumer().topic(topic).subscriptionName("BcpConsumer")
                    .receiverQueueSize(MAX_RECEIVE_MSG).autoUpdatePartitions(true).subscriptionType(SubscriptionType.Failover)
                    .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                    .subscribe();
            return "pulsar subscribe success";
        } catch (Exception e) {
            return String.format("pulsar subscribe exception : %s", e.getMessage());
        }
    }

    public Message<byte[]> receive(Boolean autoCommit) {
        try {
            Message<byte[]> msg = consumer.receive();
            if(msg!=null && autoCommit){
                consumer.acknowledge(msg.getMessageId());
            }
            return msg;
        } catch (Exception e) {
            log.error("consume msg failed. e : {}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    public void acknowledge(MessageId msgId){
        try {
            consumer.acknowledge(msgId);
        } catch (Exception e) {
            log.error("acknowledge msg failed. e : {}", ExceptionUtils.getFullStackTrace(e));
        }
    }
    public String close() {
        try {
            consumer.close();
            consumer = null;
        } catch (Exception e) {
            log.error("close consumer failed. e : {}", ExceptionUtils.getFullStackTrace(e));
        }
        try {
            pulsarClientSimulator.close();
        } catch (Exception e) {
            log.error("close pulsar client failed. e: {}", ExceptionUtils.getFullStackTrace(e));
        }
        return "success";
    }
}
