package com.bsi.utils;

import com.bsi.framework.core.httpclient.builder.HCB;
import com.bsi.framework.core.httpclient.common.*;
import com.bsi.framework.core.httpclient.utils.HttpClientUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.RequestUtils;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * http服务调用工具类
 * @author fish
 */
public class HttpUtils {

    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    /**
     * 通过restTemplate请求数据
     * @param url
     * @param headers
     * @param body
     * @return
     */
    public static AgHttpResult postByRestTemplate(String url,Map<String,String> headers, String body){
        AgHttpResult ar = new AgHttpResult();
        RestTemplate client = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        headers.forEach((k,v)->{
            header.add(k,v);
        });
        HttpEntity<String> strEntity = new HttpEntity<String>(body,header);
        ResponseEntity<String> result = client.exchange(url, HttpMethod.POST,strEntity,String.class);
        ar.setCode(result.getStatusCodeValue());
        ar.setResult(result.getBody());
        return ar;
    }

    /**
     * 通过restTemplate请求formData数据
     * @param url
     * @param headers
     * @param valueMap
     * @return
     */
    public static AgHttpResult postFormDataByRT(String url,Map<String,String> headers, Map<String,String> valueMap){
        AgHttpResult ar = new AgHttpResult();
        RestTemplate client = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        headers.forEach((k,v)->{
            header.add(k,v);
        });
        MultiValueMap<String,Object> multiValue = new LinkedMultiValueMap();
        valueMap.forEach((k,v)->{
            multiValue.add(k,v);
        });
        HttpEntity<MultiValueMap<String,Object>> multiEntity = new HttpEntity<>(multiValue,header);
        ResponseEntity<String> result = client.exchange(url, HttpMethod.POST,multiEntity,String.class);
        ar.setCode(result.getStatusCodeValue());
        ar.setResult(result.getBody());
        return ar;
    }

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
    /**
     * 重定向
     */
    public static void sendRedirect(String url){
        try {
            RequestUtils.getResponse().sendRedirect(url);
        }catch (Exception e){
            info_log.error("重定向报错:{}",ExceptionUtils.getFullStackTrace(e));
        }

    }
    
    /**
     * 调用http接口工具类
     * @param url
     * @param headers
     * @param params
     * @return AgHttpResult
     */
    public static AgHttpResult postFormWithoutCookies(String url, Map<String,String> headers, Map<String,Object> params){
        if( MapUtils.isEmpty(headers) ){
            headers = new HashMap<>();
        }
        headers.put("Content-Type","application/x-www-form-urlencoded");
        return requestWithoutCookies("POST",url,headers,params);
    }

    /**
     * 调用http接口工具类
     * @param method
     * @param url
     * @param headers
     * @param params
     * @return AgHttpResult
     */
    public static AgHttpResult requestWithoutCookies(String method, String url, Map<String,String> headers, Map<String,Object> params){
        HttpConfig config = buildHttpConfig(method,url,headers);
        HttpClient client = null;
        try{
            if (config.url().toLowerCase().startsWith("https://")) {
                client = HCB.custom().retry(2).disableCookieManagement().build();
            } else {
                client = HCB.custom().sslpv(SSLs.SSLProtocolVersion.TLSv1_2).ssl().retry(2).disableCookieManagement().build();
            }
        }catch (Exception e){
            info_log.error("初始化httpclient报错,错误信息:{}",ExceptionUtils.getFullStackTrace(e));
        }
        if( MapUtils.isNotEmpty(params) ){
            config.mapForce(params);
        }
        config.client(client);
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
            info_log.error("调用接口报错,报错信息:{}", ExceptionUtils.getFullStackTrace(e));
            result.setCode(500);
            result.setResult("error");
        }
        return result;
    }
}
