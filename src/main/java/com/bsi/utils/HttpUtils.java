package com.bsi.utils;

import com.bsi.framework.core.httpclient.builder.HCB;
import com.bsi.framework.core.httpclient.common.*;
import com.bsi.framework.core.httpclient.exception.HttpProcessException;
import com.bsi.framework.core.httpclient.utils.HttpClientUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.client.HttpClient;

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
     * @param method
     * @param url
     * @param headers
     * @param body
     * @return AgHttpResult
     */
    public static AgHttpResult request(String method, String url, Map<String,String> headers, String body){
        //插件式配置生成HttpClient时所需参数（超时、连接池、ssl、重试）
        HttpClient client = null;
        try{
            HCB hcb = HCB.custom()
                    .sslpv(SSLs.SSLProtocolVersion.TLSv1_2) 	//可设置ssl版本号，默认SSLv3，用于ssl，也可以调用sslpv("TLSv1.2")
                    .ssl()  			   		//https，支持自定义ssl证书路径和密码，ssl(String keyStorePath, String keyStorepass)
                    .pool(500, 50)    	//启用连接池，每个路由最大创建50个链接，总连接数限制为500个
                    .retry(2);					//重试2次
            client = hcb.build();
        }catch (HttpProcessException e){
            log.info("创建httpclient报错,报错信息:",ExceptionUtils.getFullStackTrace(e));
        }

        HttpConfig config = HttpConfig.simpleCustom(80000).client(client);
//        HttpConfig config = HttpConfig.simpleCustom(80000);
        HttpHeader header = HttpHeader.custom();
        if( MapUtils.isNotEmpty(headers) ){
            for( String key:headers.keySet() ){
                header.other(key,headers.get(key));
            }
        }
        config.method(HttpMethods.valueOf(method)).headers(header.build()).url(url).json(body);
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
