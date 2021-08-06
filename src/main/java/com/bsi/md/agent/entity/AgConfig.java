package com.bsi.md.agent.entity;

import com.bsi.framework.core.entity.AbstractPageEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 集成配置
 */
@Entity
@Data
@Table(name="md_agent_config")
public class AgConfig extends AbstractPageEntity {
    //名称
    private String name;
    //备注
    private String remark;
    //状态：1:已下发、0:待下发
    private String status;
    //节点id
    private String nodeId;
    //配置数据
    private String configValue;
}
