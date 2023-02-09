package com.bsi.utils;

import com.alibaba.fastjson.JSON;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author  fish
 * request工具类
 */
@Slf4j
public class HttpRequestUtils {
    /**
     * 获取路径参数
     */
    public static String getQueryParam(){
        return RequestUtils.getRequest().getQueryString();
    }

    /**
     * 获取请求头参数
     */
    public static Map<String,String> getRequestHeaders(){
        Map<String,String> headerMap = new HashMap<>();
        Header[] headers = RequestUtils.getReqHeaders();
        if( headers!=null ){
            for(Header h:headers){
                headerMap.put(h.getName(),h.getValue());
            }
        }
        return headerMap;
    }

    /**
     * 获取调用方法
     */
    public static String getMethod(){
        return RequestUtils.getRequest().getMethod();
    }

    /**
     * 设置返回headers
     * @param headers
     */
    public static void setResponseHeaders(Map<String, String> headers){
        headers.forEach( (k,v) -> RequestUtils.getResponse().setHeader(k,v));
    }
    /**
     * 设置返回码
     */
    public static void setCode(int code){
        RequestUtils.getResponse().setStatus(code);
    }

    /**
     * 获取请求body
     */
    public static String getRequestBody(){
        String body = "";
        try{
            body = RequestUtils.getRequest().getReader().lines().collect(Collectors.joining());
        }catch (Exception e){
            log.error("获取body参数报错,错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return body;
    }

    /**
     * 获取请求头参数
     */
    public static String getRequestCookies(){
        Cookie[] cookies = RequestUtils.getRequest().getCookies();
        String res = "";
        if( cookies!=null ){
            res = JSON.toJSONString(cookies);
        }
        return res;
    }
}
