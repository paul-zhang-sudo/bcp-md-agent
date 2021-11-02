package com.bsi.utils;

import com.bsi.framework.core.httpclient.utils.IoTEdgeUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fish
 *解密工具类
 */
@Slf4j
public class DecryptUtils {
    /**
     * 华为解密方法
     * @param text
     * @return String
     */
    public String decrypFromHWCloud(String text){
        String result = "";
        try{
            result = IoTEdgeUtil.getItClient().decryptDataFromCloud(text);
        }catch (Exception e){
            log.info("调用华为解密方法失败，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
       return result;
    }
}
