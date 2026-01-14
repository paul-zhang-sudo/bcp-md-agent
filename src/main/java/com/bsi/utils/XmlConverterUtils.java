package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * xml转换工具类
 */
public class XmlConverterUtils {
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    /**
     * 把xml转换成json字符串
     * @param xml
     * @return String
     */
    public static String xml2JsonStr(String xml){
        String rs = "";
        try {
            // 创建 XmlMapper 和 ObjectMapper 实例
            XmlMapper xmlMapper = new XmlMapper();
            ObjectMapper jsonMapper = new ObjectMapper();
            // 将 XML 字符串解析为 JsonNode
            JsonNode jsonNode = xmlMapper.readTree(xml);

            // 将 JsonNode 转换为 JSON 字符串
            rs = jsonNode.toString();

        } catch (Exception e) {
            info_log.info("xml转成json失败,失败信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return rs;
    }
}
