package com.bsi.md.agent.datasource;

import lombok.Data;

/**
 * api类型数据源模板
 * @author fish
 */
@Data
public class AgApiUpTemplate implements AgDataSourceTemplate{
    //访问id
    private String ak;
    //访问密钥
    private String sk;
}