package com.bsi.utils;

import com.bsi.framework.core.httpclient.common.*;
import com.bsi.framework.core.httpclient.utils.HttpClientUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * http服务调用工具类
 * @author fish
 */
@Slf4j
public class HttpUtils {

    /**
     * 调用http接口工具类
     * @param url
     * @param headers
     * @param body
     * @return AgHttpResult
     */
    public static AgHttpResult post(String url, Map<String,String> headers, String body){
        if( MapUtils.isEmpty(headers) ){
            headers = new HashMap<>();
        }
        headers.put("Content-Type","application/json");
        return request("POST",url,headers,body);
    }

    /**
     * 调用http接口工具类
     * @param url
     * @param headers
     * @param params
     * @return AgHttpResult
     */
    public static AgHttpResult postForm(String url, Map<String,String> headers, Map<String,Object> params){
        if( MapUtils.isEmpty(headers) ){
            headers = new HashMap<>();
        }
        headers.put("Content-Type","application/x-www-form-urlencoded");
        return request2("POST",url,headers,params);
    }

    /**
     * 调用http接口工具类
     * @param method
     * @param url
     * @param headers
     * @param body
     * @return AgHttpResult
     */
    public static AgHttpResult request(String method, String url, Map<String,String> headers, String body){
        HttpConfig config = buildHttpConfig(method,url,headers);
        config.json(body);
        return sendAndGetResult(config);
    }
    /**
     * 调用http接口工具类
     * @param method
     * @param url
     * @param headers
     * @param params
     * @return AgHttpResult
     */
    public static AgHttpResult request2(String method, String url, Map<String,String> headers,Map<String,Object> params){
        HttpConfig config = buildHttpConfig(method,url,headers);
        if( MapUtils.isNotEmpty(params) ){
            config.mapForce(params);
        }
        return sendAndGetResult(config);
    }

    private static HttpConfig buildHttpConfig(String method, String url, Map<String,String> headers){
        HttpConfig config = HttpConfig.simpleCustom(80000);
        HttpHeader header = HttpHeader.custom();
        if( MapUtils.isNotEmpty(headers) ){
            for( String key:headers.keySet() ){
                header.other(key,headers.get(key));
            }
        }
        config.method(HttpMethods.valueOf(method)).headers(header.build()).url(url);
        return config;
    }

    private static AgHttpResult sendAndGetResult(HttpConfig config){
        AgHttpResult result = new AgHttpResult();
        try{
            HttpResult rs = HttpClientUtil.sendAndGetResp( config,false );
            result.setCode( rs.getStatusCode() );
            result.setResp( rs.getResp() );
            result.setHeader( rs.getRespHeaders() );
            result.setResult( rs.getResult() );
        }catch (Exception e){
            log.error("调用接口报错,报错信息:{}", ExceptionUtils.getFullStackTrace(e));
            result.setCode(500);
            result.setResult("error");
        }
        return result;
    }

}
