package com.bsi.md.agent.datasource;

import com.alibaba.fastjson.JSONObject;
import com.bsi.md.agent.pulsar.PulsarClientSimulator;
import com.bsi.md.agent.pulsar.PulsarConsumerSimulator;
import com.bsi.md.agent.pulsar.PulsarProducerSimulator;
import lombok.Data;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * sapRfc类型数据源模板
 * @author fish
 */
@Data
public class AgPulsarTemplate implements AgDataSourceTemplate{
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    //kafka服务地址
    private String servers;

    //groupId
    private String groupId;

    private PulsarClientSimulator client;

    //其他参数
    private Map<String,String> otherParams;

    private Map<String, PulsarConsumerSimulator> consumerMap = new HashMap<>();
    private Map<String, PulsarProducerSimulator> producerMap = new HashMap<>();

    public AgPulsarTemplate(String servers, String groupId, Map<String,String> otherParams){
        this.servers = servers;
        this.groupId = groupId;
        this.otherParams = otherParams;
        this.client = new PulsarClientSimulator(servers,
                Boolean.parseBoolean(otherParams.get("enableTls")), Boolean.parseBoolean(otherParams.get("allowTlsInsecure")), Boolean.parseBoolean(otherParams.get("enableTlsHostNameVerification")),
                otherParams.getOrDefault("authType","NONE"), otherParams.get("keyStorePath"), otherParams.get("keyStorePassword"), otherParams.get("trustStorePath"), otherParams.get("trustStorePassword"), otherParams.get("jwtToken"));
        client.getPulsarClient();
    }

    private PulsarConsumerSimulator getConsumer(String topic){
        return new PulsarConsumerSimulator(topic,client);
    }

    private PulsarProducerSimulator getProducer(String topic){
        return new PulsarProducerSimulator(topic,client);
    }


    /**
     * 拉取指定topic的消息
     * @param key
     * @param topic
     * @return
     */
    public Object poll(String key,String topic,Boolean autoCommit){
        PulsarConsumerSimulator consumer = consumerMap.get(key);
        if(consumer==null){
            consumer = getConsumer(topic);
            consumerMap.put(key,consumer);
        }
        Message<byte[]> msg= consumer.receive(autoCommit);
        JSONObject obj = new JSONObject();
        obj.put("key",msg.getKey());
        obj.put("msgId",msg.getMessageId().toString());
        obj.put("value",new String(msg.getValue()));
        return obj.toJSONString();
    }

    /**
     * 拉取指定topic的消息
     * @param key
     * @param topic
     * @return
     */
    public Object send(String producerKey,String topic,String key,String msg){
        PulsarProducerSimulator producer = producerMap.get(producerKey);
        if(producer==null){
            producer = getProducer(topic);
            producerMap.put(producerKey,producer);
        }
        return producer.produce(msg,key);
    }

    /**
     * 确认消费
     * @param key
     * @return
     */
    public Boolean commit(String key,MessageId msgId){
        PulsarConsumerSimulator consumer = consumerMap.get(key);
        if(consumer==null){
            return false;
        }
        consumer.acknowledge(msgId);
        return true;
    }
}