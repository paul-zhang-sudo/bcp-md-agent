package com.bsi.utils;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.datasource.AgKafkaTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka工具类
 */
@Slf4j
public class KafkaUtils {

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
}
