package com.bsi.md.agent.entity.dto;

import lombok.Data;

/**
 * 集成配置 数据传输对象
 * @author fish 
 */
@Data
public class AgConfigDtoForIOT {
    private String id;
    //名称
    private String name;
    //数据源
    private String data_sources;
    //配置值
    private String config_values;
    //任务列表
    private String jobs;
}
