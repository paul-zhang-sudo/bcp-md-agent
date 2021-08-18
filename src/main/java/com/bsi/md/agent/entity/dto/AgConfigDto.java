package com.bsi.md.agent.entity.dto;

import lombok.Data;

/**
 * 集成配置 数据传输对象
 * @author fish 
 */
@Data
public class AgConfigDto {
    private Integer id;
    //名称
    private String name;
    //配置值
    private String configValue;
}
