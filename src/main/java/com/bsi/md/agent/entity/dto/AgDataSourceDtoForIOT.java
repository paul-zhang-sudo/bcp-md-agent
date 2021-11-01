package com.bsi.md.agent.entity.dto;

import lombok.Data;

/**
 * 数据源 数据传输对象
 * @author fish
 */
@Data
public class AgDataSourceDtoForIOT {
    //数据源id
    private String id;
    //名称
    private String name;
    //配置值
    private String config_values;
    //是否删除
    private Boolean del_flag;
}
