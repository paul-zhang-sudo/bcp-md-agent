package com.bsi.md.agent.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 集成配置
 */
@Entity
@Data
@Table(name="md_agent_config")
public class AgConfig extends AgAbstractEntity {
    //名称
    private String name;
    //配置数据
    @Column(name = "configValue",columnDefinition="ntext")
    private String configValue;
}
