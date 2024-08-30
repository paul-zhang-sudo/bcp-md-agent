package com.bsi.md.agent.utils;


import com.bsi.framework.core.utils.ExceptionUtils;
import com.huaweicloud.sdk.iot.module.DriverClient;
import com.huaweicloud.sdk.iot.module.ItClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;

/**
 * @author fish
 * IoTEdge工具类
 */
@Slf4j
public class IoTEdgeUtil {
    private static ItClient itClient = null;

    private static DriverClient driverClient = null;

    static {
        try {
            itClient = ItClient.createFromEnv();
            itClient.open();
        } catch (Exception e) {
            log.error("初始化itClient报错，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }

        try {
            driverClient = DriverClient.createFromEnv();
            driverClient.open();
        } catch (Exception e) {
            log.error("初始化driverClient报错，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
    }

    public static DriverClient getDriverClient(){
        return driverClient;
    }
    /**
     * 获取httpClient
     * @return
     */
    public static HttpClient getHttpClient(){
        return itClient.getHttpClient();
    }

    /**
     * 获取ItClient对象
     * @return
     */
    public static ItClient getItClient(){
        return itClient;
    }
}
