package com.bsi.md.agent.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源vo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgDataSourceVo {
    //名称
    private String name;
    //类型
    private String type;
    //分类
    private String classify;
    //配置数据json串
    private String configValue;
}
