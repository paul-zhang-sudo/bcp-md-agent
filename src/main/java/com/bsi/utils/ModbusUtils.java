package com.bsi.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.utils.IoTEdgeUtil;
import com.huaweicloud.sdk.iot.module.dto.DeviceService;
import com.huaweicloud.sdk.iot.module.dto.ServiceData;
import com.huaweicloud.sdk.iot.module.dto.SubDevicesPropsReport;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Modbus工具类
 * 发送数据到工业物联平台2.0模型实例中
 */
@Slf4j
public class ModbusUtils {
    /**
     * 向指定设备发送消息
     *
     * @param deviceId 设备ID，用于标识目标设备
     * @param msg 要发送的消息，应为JSON格式字符串，包含服务数据
     * @return boolean 表示消息是否发送成功
     */
    public static boolean sendMsg(String deviceId,String msg){
        boolean succ = true;
        try {
            // 解析消息中的服务列表
            JSONArray servicesList = JSON.parseArray(msg);
            // 准备存储设备服务信息的列表
            List<DeviceService> devices = new LinkedList<>();
            // 准备存储服务数据的列表
            List<ServiceData> services = new LinkedList<>();

            // 遍历服务列表，解析每个服务的详细信息
            for (int i = 0; i < servicesList.size(); i++) {
                JSONObject p = servicesList.getJSONObject(i);
                // 存储服务的属性信息
                Map<String, Object> properties = new HashMap<>();

                // 根据服务ID和服务属性创建服务数据对象，并记录当前时间
                ServiceData serviceData = new ServiceData(p.getString("serviceId"), p.getJSONObject("properties"), ZonedDateTime.now());
                services.add(serviceData);
                // 创建设备服务对象，关联设备ID和服务数据
                DeviceService deviceService = new DeviceService(deviceId, services);
                devices.add(deviceService);
            }

            // 创建子设备属性报告对象，包含所有设备的服务信息
            SubDevicesPropsReport report = new SubDevicesPropsReport(devices);
            // 使用IoT Edge工具类获取客户端，并上报子设备属性
            IoTEdgeUtil.getDriverClient().reportSubDevicesProperties(report);
        } catch (Exception e) {
            // 异常处理：设置发送失败标志
            succ = false;
            // 记录错误日志，输出异常堆栈信息
            log.error("发送数据到iot报错,错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        // 返回发送状态
        return succ;
    }
}
