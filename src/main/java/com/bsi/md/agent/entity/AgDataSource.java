package com.bsi.md.agent.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 数据源
 */
@Entity
@Data
@Table(name="md_agent_datasource")
public class AgDataSource extends AgAbstractEntity {
    //名称
    private String name;
    //类型
    private String type;
    //分类
    private String classify;
    //配置数据json串
    @Column(name = "configValue",columnDefinition="ntext")
    private String configValue;

}
