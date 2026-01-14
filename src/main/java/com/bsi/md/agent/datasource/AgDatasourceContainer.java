package com.bsi.md.agent.datasource;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据源存储容器
 * @author fish
 */
public class AgDatasourceContainer {
    private static final Map<String,AgJdbcTemplate> jdbcMap = new HashMap<>();
    private static final Map<String, AgApiTemplate> apiMap = new HashMap<>();
    private static final Map<String, AgApiUpTemplate> apiUpMap = new HashMap<>();
    private static final Map<String, AgSapRFCTemplate> sapRfcMap = new HashMap<>();
    private static final Map<String, JSONObject> propertiesMap = new HashMap<>();
    private static final Map<String, AgKafkaTemplate> kafkaMap = new HashMap<>();
    private static final Map<String, AgPulsarTemplate> pulsarMap = new HashMap<>();
    private static final Map<String, AgMqttTemplate> mqttMap = new HashMap<>();

    /**
     * 获取数据源属性
     * @param key
     * @return JSONObject
     */
    public static JSONObject getDSProperties(String key){
        return propertiesMap.get(key);
    }

    /**
     * 设置数据源属性
     * @param key
     * @param prop
     */
    public static void setDSProperties(String key,JSONObject prop){
        propertiesMap.put(key,prop);
    }
    /**
     * 添加一个jdbc数据源
     * @param key
     * @param template
     */
    public static void addJdbcDataSource(String key,AgJdbcTemplate template){
        jdbcMap.put(key,template);
    }

    /**
     * 清空jdbc数据源map
     */
    public static void clearJdbcDataSource(){
        jdbcMap.clear();
    }

    /**
     * 清空api数据源map
     */
    public static void clearApiDataSource(){
        apiMap.clear();
    }

    /**
     * 清空api数据源map
     */
    public static void clearApiUpDataSource(){
        apiMap.clear();
    }
    /**
     * 清空rfc数据源map
     */
    public static void clearSapRfcDataSource(){
        sapRfcMap.clear();
    }
    /**
     * 清空jdbc数据源map
     */
    public static void clearKafkaDataSource(){
        kafkaMap.clear();
    }
    /**
     * 清空pulsar数据源map
     */
    public static void clearPulsarDataSource(){
        pulsarMap.clear();
    }
    /**
     * 清空mqtt数据源map
     */
    public static void clearMqttDataSource(){
        mqttMap.values().forEach(AgMqttTemplate::close);
        mqttMap.clear();
    }
    /**
     * 添加一个jdbc数据源
     * @param key
     * @param template
     */
    public static void addApiUpDataSource(String key,AgApiUpTemplate template){
        apiUpMap.put(key,template);
    }

    /**
     * 获取jdbc数据源
     * @param key
     * @return
     */
    public static AgApiUpTemplate getApiUpDataSource(String key){
        return apiUpMap.get(key);
    }

    /**
     * 清空所有
     */
    public static void clearAllDataSource(){
        clearApiDataSource();
        clearApiUpDataSource();
        clearJdbcDataSource();
        clearSapRfcDataSource();
        clearKafkaDataSource();
        clearPulsarDataSource();
        clearMqttDataSource();
        propertiesMap.clear();
    }

    /**
     * 获取jdbc数据源
     * @param key
     * @return
     */
    public static AgJdbcTemplate getJdbcDataSource(String key){
        return jdbcMap.get(key);
    }

    /**
     * 添加一个api数据源
     * @param key
     * @param template
     */
    public static void addApiDataSource(String key,AgApiTemplate template){
        apiMap.put(key,template);
    }

    /**
     * 获取api数据源
     * @param key
     * @return
     */
    public static AgApiTemplate getApiDataSource(String key){
        return apiMap.get(key);
    }
    /**
     * 获取kafka数据源
     * @param key
     * @return
     */
    public static AgKafkaTemplate getKafkaDataSource(String key){
        return kafkaMap.get(key);
    }
    /**
     * 获取pulsar数据源
     * @param key
     * @return
     */
    public static AgPulsarTemplate getPulsarDataSource(String key){
        return pulsarMap.get(key);
    }
    /**
     * 添加一个sapRFC数据源
     * @param key
     * @param template
     */
    public static void addSapRfcDataSource(String key,AgSapRFCTemplate template){
        sapRfcMap.put(key,template);
    }
    /**
     * 添加一个kafka数据源
     * @param key
     * @param template
     */
    public static void addKafkaDataSource(String key,AgKafkaTemplate template){
        kafkaMap.put(key,template);
    }
    /**
     * 添加一个pulsar数据源
     * @param key
     * @param template
     */
    public static void addPulsarDataSource(String key,AgPulsarTemplate template){
        pulsarMap.put(key,template);
    }
    /**
     * 添加一个mqtt数据源
     * @param key
     * @param template
     */
    public static void addMqttDataSource(String key,AgMqttTemplate template){
        mqttMap.put(key,template);
    }

    /**
     * 获取sapRFC数据源
     * @param key
     * @return
     */
    public static AgSapRFCTemplate getSapRfcDataSource(String key){
        return sapRfcMap.get(key);
    }

    /**
     * 获取mqtt数据源
     * @param key
     * @return
     */
    public static AgMqttTemplate getMqttDataSource(String key){
        return mqttMap.get(key);
    }
}
