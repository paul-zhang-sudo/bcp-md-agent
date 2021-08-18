package com.bsi.md.agent.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节点
 * @author fish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgNodeVo {
    //节点类型
    private String type;
    //节点分类
    private String classify;
    //节点脚本
    private String script;
    //节点类名
    private String className;
    //数据源id
    private String dataSourceId;


}
