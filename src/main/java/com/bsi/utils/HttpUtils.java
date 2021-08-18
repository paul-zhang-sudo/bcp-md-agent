package com.bsi.utils;

import com.bsi.framework.core.httpclient.common.HttpConfig;
import com.bsi.framework.core.httpclient.common.HttpHeader;
import com.bsi.framework.core.httpclient.common.HttpMethods;
import com.bsi.framework.core.httpclient.common.HttpResult;
import com.bsi.framework.core.httpclient.utils.HttpClientUtil;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * http服务调用工具类
 * @author fish
 */
@Slf4j
public class HttpUtils {

    /**
     * 调用http接口工具类
     * @param method
     * @param url
     * @param headers
     * @param body
     * @return
     */
    public AgHttpResult request(String method, String url, Map<String,String> headers, String body){
        HttpConfig config = HttpConfig.simpleCustom(12000);
        if( MapUtils.isNotEmpty(headers) ){
            for( String key:headers.keySet() ){
                HttpHeader.custom().other(key,headers.get(key));
            }
        }
        config.method(HttpMethods.valueOf(method)).url(url).json(body);
        AgHttpResult result = new AgHttpResult();
        try{
            HttpResult rs = HttpClientUtil.sendAndGetResp( config,false );
            result.setCode( rs.getStatusCode() );
            result.setResp( rs.getResp() );
            result.setHeader( rs.getRespHeaders() );
            result.setResult( rs.getResult() );
        }catch (Exception e){
            log.error("调用接口报错,报错信息:{}");
            result.setCode(500);
            result.setResult("error");
        }
        return result;
    }


}
