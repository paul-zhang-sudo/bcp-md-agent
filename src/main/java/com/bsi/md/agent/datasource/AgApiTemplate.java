package com.bsi.md.agent.datasource;

import lombok.Data;

/**
 * api类型数据源模板
 * @author fish
 */
@Data
public class AgApiTemplate implements AgDataSourceTemplate{
    //api地址
    private String apiUrl;

    //认证模式 (none:无认证，token:token认证)
    private String authType;

    //token地址
    private String authUrl;

    //token参数
    private String authParam;

    //token方法
    private String authMethod;
}