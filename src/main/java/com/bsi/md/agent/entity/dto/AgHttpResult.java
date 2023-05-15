package com.bsi.md.agent.entity.dto;

import lombok.Data;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.util.Arrays;
import java.util.Optional;

/**
 * http结果返回值
 */
@Data
public class AgHttpResult {
    private Integer code;
    private String result;
    private byte[] byteResult;
    private Header[] header;
    private HttpResponse resp;

    /**
     * 获取制定名称的header
     * @param headerName
     * @return
     */
    public Header getHeader(String headerName){
        Header[] headers = this.resp.getHeaders(headerName);
        return headers != null && headers.length > 0 ? headers[0] : null;
    }

    public Header getHeaderFromHeaders(String headerName){
        Optional<Header> resHeader = Arrays.stream(header).filter(v-> headerName.equals(v.getName())).findFirst();
        return resHeader.orElse(null);
    }
}
