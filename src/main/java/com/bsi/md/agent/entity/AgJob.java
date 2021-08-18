package com.bsi.md.agent.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * api仓库
 */
@Entity
@Data
@Table(name="md_agent_job")
public class AgJob extends AgAbstractEntity {
    //所属配置id
    private Integer configId;
    //名称
    private String name;
    //cron表达式
    private String cron;
    //配置值
    //配置数据
    @Column(name = "configValue",columnDefinition="ntext")
    private String configValue;
}
