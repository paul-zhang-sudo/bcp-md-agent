package com.bsi.md.agent.entity.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Map;

/**
 * 集成配置vo类
 * @author fish
 */
@Data
public class AgIntegrationConfigVo {
    //配置id
    private String configId;

    //任务id
    private String taskId;
    //任务名称
    private String taskName;
    //任务参数配置
    private Map<String,Object> paramMap;

    //输入节点
    private JSONObject inputNode;

    //转换节点类型
    private JSONObject transformNode;

    //输出节点类型
    private JSONObject outputNode;

}
