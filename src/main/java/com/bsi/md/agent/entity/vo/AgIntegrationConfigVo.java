package com.bsi.md.agent.entity.vo;

import lombok.Data;

import java.util.Map;

/**
 * 集成配置vo类
 * @author fish
 */
@Data
public class AgIntegrationConfigVo {
    //配置id
    private Integer configId;

    //任务id
    private Integer taskId;
    //任务参数配置
    private Map<String,Object> paramMap;

    //输入节点
    private AgNodeVo inputNode;

    //转换节点类型
    private AgNodeVo transformNode;

    //输出节点类型
    private AgNodeVo outputNode;

}
