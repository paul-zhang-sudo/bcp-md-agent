package com.bsi.md.agent.log;

import com.bsi.framework.core.utils.IpUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgTaskLog {
    private String clientIp; //客户端地址
    private String taskCode; //计划任务编码
    private String taskName;  //计划任务名称
    private String timeLocal; //执行时间
    private String execTime; //执行时长
    private String result; //执行结果
    private int validSize; //源端数据条数
    private int successSize; //成功条数
    private int failSize; //失败条数
    private String errorId; //错误日志id

    public String getClientIp() {
        if( StringUtils.isEmpty(clientIp) ){
            clientIp = IpUtils.INTERNET_IP;
        }
        return clientIp;
    }
}
