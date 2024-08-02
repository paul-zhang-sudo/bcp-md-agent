package com.bsi.utils;

import com.bsi.framework.core.utils.StringUtils;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.datasource.AgPulsarTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.impl.MessageIdImpl;

@Slf4j
public class PulsarUtils {
    /**
     * 根据主题查询数据
     * @param dataSourceId 数据源id
     * @param topic 主题
     */
    public static Object poll(String dataSourceId,String taskId,String topic,Boolean autoCommit){
        AgPulsarTemplate template = AgDatasourceContainer.getPulsarDataSource(dataSourceId);
        return template.poll(dataSourceId+"-"+taskId,topic,autoCommit);
    }

    /**
     * 根据主题批量拉取数据
     * @param dataSourceId 数据源id
     * @param topic 主题
     * @param receiverQueueSize 接收队列大小
     * @param maxNumMessages  每次接收消息最大条数
     * @param  maxNumBytes 每次接收消息最大字节
     */
    public static Object batchPoll(String dataSourceId,String taskId,String topic,int receiverQueueSize,int maxNumMessages,int maxNumBytes,int pullTimeout,int ackTimeout){
        AgPulsarTemplate template = AgDatasourceContainer.getPulsarDataSource(dataSourceId);
        return template.pollMany(dataSourceId+"-"+taskId,topic,receiverQueueSize,maxNumMessages,maxNumBytes,pullTimeout,ackTimeout);
    }

    /**
     * 根据主题查询数据
     * @param dataSourceId 数据源id
     * @param topic 主题
     */
    public static Object send(String dataSourceId,String taskId,String topic,String key,String msg){
        AgPulsarTemplate template = AgDatasourceContainer.getPulsarDataSource(dataSourceId);
        return template.send(dataSourceId+"-"+taskId,topic,key,msg);
    }

    /**
     * 确认消费
     * @param dataSourceId
     * @param taskId
     * @param msgId
     */
    public static void commit(String dataSourceId, String taskId, String msgId){
        AgPulsarTemplate template = AgDatasourceContainer.getPulsarDataSource(dataSourceId);
        String[] arr = StringUtils.split(msgId,":");
        MessageIdImpl idImpl = new MessageIdImpl(Long.parseLong(arr[0]),Long.parseLong(arr[1]),Integer.parseInt(arr[2]));
        template.commit(dataSourceId+"-"+taskId,idImpl);
    }
}
