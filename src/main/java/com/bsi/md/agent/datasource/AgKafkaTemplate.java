package com.bsi.md.agent.datasource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * sapRfc类型数据源模板
 * @author fish
 */
@Data
public class AgKafkaTemplate implements AgDataSourceTemplate{
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    //kafka服务地址
    private String servers;

    //groupId
    private String groupId;

    //自动提交
    private String autoCommit;

    //自动提交时间
    private String autoCommitInterval;

    private String autoOffset;

    //
    private String keyDecode = "org.apache.kafka.common.serialization.StringDeserializer";

    //密码
    private String classDecode = "org.apache.kafka.common.serialization.StringDeserializer";

    //其他参数
    private Map<String,String> otherParams;

    //jco
    private Map<String,KafkaConsumer> consumerMap = new HashMap<>();

    public AgKafkaTemplate(String servers, String groupId, String autoCommit, String autoCommitInterval,String autoOffset ,String keyDecode, String classDecode, Map<String,String> otherParams){
        this.servers = servers;
        this.groupId = groupId;
        this.autoCommit = autoCommit;
        this.autoCommitInterval = autoCommitInterval;
        this.autoOffset = autoOffset;
        this.keyDecode = keyDecode;
        this.classDecode = classDecode;
        this.otherParams = otherParams;
    }

    private KafkaConsumer getConsumer(){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.servers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, this.autoCommit);
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, this.autoCommitInterval);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.autoOffset);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, this.keyDecode);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, this.classDecode);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,100); //一次最多处理100条
        if( MapUtils.isNotEmpty(otherParams) ){
            otherParams.forEach((k,v)-> properties.setProperty(k,v) );
        }
        return new KafkaConsumer<>(properties);
    }

    /**
     * 拉取指定topic的消息
     * @param topic
     * @return
     */
    public Object poll(String key,String topic){
        return poll(key,topic,1000);
    }

    /**
     * 拉取指定topic的消息
     * @param topic
     * @param timeOut 超时多久关闭连接
     * @return
     */
    public Object poll(String key,String topic,long timeOut){
        return poll(key,topic,timeOut,false);
    }
    /**
     * 拉取指定topic的消息
     * @param topic
     * @param timeOut 超时多久关闭连接
     * @return
     */
    private Object poll(String key,String topic,long timeOut,boolean reSubscribe){
        JSONArray result = new JSONArray();
        KafkaConsumer consumer = consumerMap.get(key);
        if(consumer==null){
            consumer = getConsumer();
            consumerMap.put(key,consumer);
            if(!reSubscribe){
                consumer.subscribe(Arrays.asList(topic.split(",")));
            }
        }
        if(reSubscribe){
             consumer.unsubscribe(); //unsubscribe之后，可以根据offset来消费
             consumer.subscribe(Arrays.asList(topic.split(",")));
        }
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(timeOut));
        for (ConsumerRecord<String, String> record : records) {
            JSONObject obj = new JSONObject();
            obj.put("key",record.key());
            obj.put("offset",record.offset());
            obj.put("value",record.value());
            result.add(obj);
        }
        return result.toJSONString();
    }

    /**
     * 根据偏移量来拉取数据
     * @param key
     * @param topic
     * @param timeOut
     * @return
     */
    public Object pollByOffset(String key,String topic,long timeOut){
        return poll(key,topic,timeOut,true);
    }

    /**
     * 确认消费
     * @param key
     * @return
     */
    public Boolean commit(String key){
        KafkaConsumer consumer = consumerMap.get(key);
        if(consumer==null){
            return false;
        }
        consumer.commitSync(Duration.ofMillis(2000));
        return true;
    }
}