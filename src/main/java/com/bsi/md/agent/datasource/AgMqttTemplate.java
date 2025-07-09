package com.bsi.md.agent.datasource;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.StringUtils;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import com.bsi.md.agent.executor.AgExecutorService;
import com.bsi.utils.HttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * sapRfc类型数据源模板
 * @author fish
 */
@Data
@Slf4j
public class AgMqttTemplate implements AgDataSourceTemplate{
    //mqtt服务地址
    private String servers;

    //groupId
    private String groupId;

    private MqttClient mqttClient;

    //其他参数
    private Map<String,String> otherParams;

    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    public AgMqttTemplate(String servers, String groupId, Map<String,String> otherParams){
        this.servers = servers;
        this.groupId = groupId;
        this.otherParams = otherParams;

        //异步初始化，否则影响程序运行
        AgExecutorService.getExecutor().submit(() -> {
            this.mqttClient = getClient();
        });

    }

    private MqttClient getClient(){
        log.info("初始化mqtt数据源...");
        MqttClient client = null;
        try{
            //延迟2秒初始化，先让执行引擎初始化完毕
            Thread.sleep(2000L);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            if(this.otherParams.containsKey("uname")){
                options.setUserName(this.otherParams.get("uname"));
            }
            if(this.otherParams.containsKey("password")){
                options.setPassword(this.otherParams.get("password").toCharArray());
            }
            client = new MqttClient(this.servers,StringUtils.hasText(this.groupId)?this.groupId:MqttClient.generateClientId());
            client.connect(options);
            log.info("mqtt connect {}",client.isConnected());
            client.subscribe(StringUtils.split(this.otherParams.get("topics"),","));
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    log.error("mqtt lost connection ...");
                    // 连接丢失
                }
                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    // 收到消息
                    try {
                        String msg = new String(mqttMessage.getPayload());
                        log.info("receive msg:{}",msg);
                        AgHttpResult res = HttpUtils.post("http://127.0.0.1:8080/api/"+topic,null,msg);
                    }catch (Exception e){
                        log.error("send msg error:{}",ExceptionUtils.getFullStackTrace(e));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    // 消息发布完成
                    //log.info("msg send success ...");
                }
            });
        }catch (Exception e){
            log.info("connect mqtt broken error:{}", ExceptionUtils.getFullStackTrace(e));
        }
        log.info("mqtt数据源初始化完毕...");
        return client;
    }

    /**
     * 关闭client
     */
    public void close(){
        if(this.mqttClient!=null){
            try{
                this.mqttClient.disconnectForcibly();
                this.mqttClient.close(true);
                this.mqttClient = null;
            }catch (Exception e) {}
        }
    }

    /**
     * 发送消息到MQTT Broker
     *
     * @param topic   主题
     * @param payload 消息内容
     * @param qos     QoS等级（0:最多一次, 1:至少一次, 2:恰好一次）
     */
    public void publish(String topic, String payload, int qos) {
        if (this.mqttClient == null || !this.mqttClient.isConnected()) {
            info_log.error("MQTT未连接，topic: {}", topic);
            return;
        }
        try {
            this.mqttClient.publish(topic, payload.getBytes(), qos, false);
        } catch (MqttException e) {
            info_log.error("发送消息到topic: {}. 错误: {}", topic, ExceptionUtils.getFullStackTrace(e));
        }
    }
}