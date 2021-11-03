package com.bsi.md.agent.entity.dto;

import lombok.Data;

/**
 * 数据源 数据传输对象
 * @author fish
 */
@Data
public class AgDataSourceDto {
    //数据源id
    private String id;
    //名称
    private String name;
    //类型
    private String type;
    //分类
    private String classify;
    //配置值
    private String configValue;
    //删除标志
    private Boolean delFlag;
}
