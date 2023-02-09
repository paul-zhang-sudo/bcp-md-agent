package com.bsi.utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.MathUtils;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.datasource.AgKafkaTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Kafka工具类
 */
@Slf4j
public class KafkaUtils {
    public static KafkaConsumer<String, String> getDefaultKafkaConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.137.85.194:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "fish");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,1000); //一次最多处理100条
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(properties);
    }

    /**
     * 根据主题查询数据
     * @param dataSourceId 数据源id
     * @param topic 主题
     */
    public static Object poll(String dataSourceId,String taskId,String topic){
        AgKafkaTemplate template = AgDatasourceContainer.getKafkaDataSource(dataSourceId);
        return template.poll(dataSourceId+"-"+taskId,topic);
    }

    /**
     * 根据主题查询数据
     * @param dataSourceId 数据源id
     * @param topic 主题
     * @param timeOut 超时时间
     */
    public static Object poll(String dataSourceId,String taskId,String topic,long timeOut){
        AgKafkaTemplate template = AgDatasourceContainer.getKafkaDataSource(dataSourceId);
        return template.poll(dataSourceId+"-"+taskId,topic,timeOut);
    }

    /**
     * 手动提交offset
     * @param dataSourceId
     * @param taskId
     * @return
     */
    public static boolean commit(String dataSourceId,String taskId){
        AgKafkaTemplate template = AgDatasourceContainer.getKafkaDataSource(dataSourceId);
        return template.commit(dataSourceId+"-"+taskId);
    }
    public static void main(String[] args) {
        try {
//            KafkaConsumer<String, String> consumer = getDefaultKafkaConsumer();
//            consumer.subscribe(Arrays.asList("measure_machine_3d3p"));
//            //while (Boolean.TRUE) {
//            consumer.poll(100);
//            for(TopicPartition t: consumer.assignment()){
//                consumer.seek(new TopicPartition("measure_machine_3d3p",0),507676);
//                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
//                for (ConsumerRecord<String, String> record : records) {
//                    JSONObject obj = JSON.parseObject(record.value());
//                    if(obj.getInteger("mac_type")==3){
//                        log.info(">>>>>>>>Consumer offset:{}, key:{},value:{}", record.offset(),record.key(), record.value());
//                    }
//                }
//            }

            //测试线程池
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(1);
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Integer.valueOf(1), Integer.valueOf(1),
                    Long.valueOf(1000), TimeUnit.MILLISECONDS, queue);

            threadPoolExecutor.submit(()->{
                try {
                    Thread.sleep(1000L);
                    log.info("1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            log.info("核心线程数：{},活动线程数：{},最大线程数：{},线程池活跃度：{},任务完成数：{}," +
                            "队列大小：{},当前排队线程数：{},队列剩余大小：{},队列使用度：{}",
                    threadPoolExecutor.getCorePoolSize(),
                    threadPoolExecutor.getActiveCount(),
                    threadPoolExecutor.getMaximumPoolSize(),
                    threadPoolExecutor.getActiveCount()/threadPoolExecutor.getMaximumPoolSize(),
                    threadPoolExecutor.getCompletedTaskCount(),
                    (queue.size() + queue.remainingCapacity()),
                    queue.size(),
                    queue.remainingCapacity(),
                    queue.size()/(queue.size() + queue.remainingCapacity()));

            threadPoolExecutor.setCorePoolSize(2);
            threadPoolExecutor.setMaximumPoolSize(3);

            threadPoolExecutor.submit(()->{
                try {
                    Thread.sleep(1000L);
                    log.info("2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            log.info("核心线程数：{},活动线程数：{},最大线程数：{},线程池活跃度：{},任务完成数：{}," +
                            "队列大小：{},当前排队线程数：{},队列剩余大小：{},队列使用度：{}",
                    threadPoolExecutor.getCorePoolSize(),
                    threadPoolExecutor.getActiveCount(),
                    threadPoolExecutor.getMaximumPoolSize(),
                    threadPoolExecutor.getActiveCount()/threadPoolExecutor.getMaximumPoolSize(),
                    threadPoolExecutor.getCompletedTaskCount(),
                    (queue.size() + queue.remainingCapacity()),
                    queue.size(),
                    queue.remainingCapacity(),
                    MathUtils.div( Double.parseDouble( queue.size()+"" ),Double.parseDouble( (queue.size() + queue.remainingCapacity())+"") ) );
            //}
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}
