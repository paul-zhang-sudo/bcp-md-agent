package com.bsi.md.agent.datasource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
    private String valueDecode = "org.apache.kafka.common.serialization.StringSerializer";
    //
    private String keyDecode = "org.apache.kafka.common.serialization.StringDeserializer";

    //密码
    private String classDecode = "org.apache.kafka.common.serialization.StringDeserializer";

    //其他参数
    private Map<String,String> otherParams;

    //jco
    private Map<String,KafkaConsumer> consumerMap = new HashMap<>();

    private Map<String, KafkaProducer> producerMap = new HashMap<>();

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
    private KafkaProducer getProducer(){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.servers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, this.groupId);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, this.valueDecode);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, this.valueDecode);
        if( MapUtils.isNotEmpty(otherParams) ){
            otherParams.forEach((k,v)-> properties.setProperty(k,v) );
        }
        return new KafkaProducer(properties);
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
     * 发送kafka消息
     * @param key
     * @param topic
     * @return
     */
    public Object send(String producerKey,String topic,String key,String msg){
        KafkaProducer producer = producerMap.get(producerKey);
        if(producer==null){
            producer = getProducer();
            producerMap.put(producerKey,producer);
        }
        ProducerRecord producerRecord = new ProducerRecord<>(topic,key,msg);
        Future future = producer.send(producerRecord);
        String result = "";
        try {
            RecordMetadata recordMetadata = (RecordMetadata) future.get();
            result = String.format("send msg success. topic: %s ,offset: %s, partition: %s", recordMetadata.topic(),recordMetadata.offset(),recordMetadata.partition());
        }catch (Exception e){
            info_log.info("kafka消息发送失败:{}", ExceptionUtils.getFullStackTrace(e));
            result = "send msg fail.";
        }
        return result;
    }
    /**
     * 按照时间范围读取历史数据
     * @param topic
     * @return
     */
    public Object poll(String key,String topic,long from, long to){
        JSONArray result = new JSONArray();
        KafkaConsumer consumer = consumerMap.get(key);
        if(consumer==null){
            consumer = getConsumer();
            consumerMap.put(key,consumer);
            //consumer.subscribe(Arrays.asList(topic.split(",")));
        }
        List<PartitionInfo> partitions = consumer.partitionsFor(topic);
        // 构建TopicPartition集合
        Set<TopicPartition> topicPartitions = partitions.stream()
                .map(pi -> new TopicPartition(pi.topic(), pi.partition()))
                .collect(Collectors.toSet());
        consumer.assign(topicPartitions);

// 查询时间范围内的offset,返回 OffsetAndTimestamp
        Map<TopicPartition, Long> fromTimestamps = topicPartitions.stream().collect(Collectors.toMap(k->k,v->from));
        Map<TopicPartition, Long> toTimestamps = topicPartitions.stream().collect(Collectors.toMap(k->k,v->to));
        Map<TopicPartition, OffsetAndTimestamp> fromOffsets = consumer.offsetsForTimes(fromTimestamps);
        Map<TopicPartition, OffsetAndTimestamp> toOffsets = consumer.offsetsForTimes(toTimestamps);

        for (TopicPartition tp : fromOffsets.keySet()) {
            //订阅分区并查询指定offset范围的数据
            long fromOffset = fromOffsets.get(tp).offset();
            long toOffset = toOffsets.get(tp)==null?(Long)consumer.endOffsets(Collections.singleton(tp)).get(tp):toOffsets.get(tp).offset();
            consumer.seek(tp, fromOffset);
            while (consumer.position(tp) < toOffset) {
                ConsumerRecords<String, String> crs = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : crs) {
                    if(toOffset!=0 && record.offset()>toOffset){
                        break;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("key",record.key());
                    obj.put("offset",record.offset());
                    obj.put("value",record.value());
                    result.add(obj);
                }
            }
        }
        return result.toJSONString();
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