package com.bsi.md.agent.entity;

import com.bsi.framework.core.entity.AbstractPageEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 数据源
 */
@Entity
@Data
@Table(name="md_agent_datasource")
public class AgDataSource extends AbstractPageEntity {
    //业务场景名称
    private String name;
    //类型
    private String type;
    //分类
    private String classify;
    //备注
    private String remark;
    //节点id
    private String nodeId;
    //配置数据json串
    private String configValue;

}
