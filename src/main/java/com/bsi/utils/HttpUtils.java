package com.bsi.utils;

import com.bsi.framework.core.httpclient.builder.HCB;
import com.bsi.framework.core.httpclient.common.*;
import com.bsi.framework.core.httpclient.utils.HttpClientUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.RequestUtils;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
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
     * 通过restTemplate请求数据
     * @param url
     * @param headers
     * @param body
     * @return
     */
    public static AgHttpResult requestByRestTemplate(String method,String url,Map<String,String> headers, String body){
        AgHttpResult ar = new AgHttpResult();
        RestTemplate client = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        headers.forEach(header::add);
        HttpEntity<String> strEntity = new HttpEntity<String>(body,header);
        ResponseEntity<String> result = client.exchange(url, HttpMethod.valueOf(method),strEntity,String.class);
        ar.setCode(result.getStatusCodeValue());
        ar.setResult(result.getBody());
        return ar;
    }

    /**
     * 通过restTemplate请求数据
     * @param url
     * @param headers
     * @param body
     * @return
     */
    public static AgHttpResult requestByRestTemplateHttps(String method,String url,Map<String,String> headers, String body) {
        AgHttpResult ar = new AgHttpResult();
        try{
            RestTemplate client = new RestTemplate();
            client.setRequestFactory(new HttpComponentsClientHttpRequestFactory(
                    HttpClientBuilder.create()
                            .setSSLContext(SSLContextBuilder.create()
                                    .loadTrustMaterial(new TrustAllStrategy())
                                    .build())
                            .setSSLHostnameVerifier(new NoopHostnameVerifier())
                            .build()));
            HttpHeaders header = new HttpHeaders();
            headers.forEach(header::add);
            HttpEntity<String> strEntity = new HttpEntity<>(body, header);
            ResponseEntity<String> result = client.exchange(url, HttpMethod.valueOf(method),strEntity,String.class);
            ar.setCode(result.getStatusCodeValue());
            ar.setResult(result.getBody());

            HttpHeader resHeader = HttpHeader.custom();
            if( MapUtils.isNotEmpty(result.getHeaders()) ){
                for(Map.Entry<String, List<String>> entrySet : result.getHeaders().entrySet()){
                    resHeader.other(entrySet.getKey(),entrySet.getValue().size() > 0 ? entrySet.getValue().get(0) : null);
                }
            }
            ar.setHeader(resHeader.build());
        }catch (HttpClientErrorException | HttpServerErrorException e){
            ar.setCode(e.getStatusCode().value());
            ar.setResult(e.getResponseBodyAsString());
        }catch (ResourceAccessException e){
            ar.setCode(HttpStatus.REQUEST_TIMEOUT.value());
            ar.setResult(HttpStatus.REQUEST_TIMEOUT.getReasonPhrase());
        }catch (Exception e){
            ar.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            ar.setResult(e.getMessage());
        }
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

    /**
     * 调用http接口工具类
     * @param url
     * @param headers
     * @param body
     * @return AgHttpResult
     */
    public static AgHttpResult postForStream(String url, Map<String,String> headers, String body){
        if( MapUtils.isEmpty(headers) ){
            headers = new HashMap<>();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        headers.put("Content-Type","application/json");
        HttpConfig config = buildHttpConfig("POST",url,headers);
        config.out(out).json(body).headers(config.headers(),true);
        AgHttpResult ar = new AgHttpResult();
        try{
            HttpClientUtil.down(config);
            ar.setCode(200);
            ar.setHeader(config.headers());
            ar.setByteResult(out.toByteArray());
            out.flush();
            out.close();
        }catch (Exception e){
            info_log.error("请求流接口报错:{}",ExceptionUtils.getFullStackTrace(e));
            ar.setCode(500);
        }

        return ar;
    }

}
