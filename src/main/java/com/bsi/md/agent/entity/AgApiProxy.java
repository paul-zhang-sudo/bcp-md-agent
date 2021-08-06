package com.bsi.md.agent.entity;

import com.bsi.framework.core.entity.AbstractPageEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 实时接口
 */
@Entity
@Data
@Table(name="md_agent_api_proxy")
public class AgApiProxy extends AbstractPageEntity {
    //所属配置id
    private Long configId;
    //名称
    private String name;
}
