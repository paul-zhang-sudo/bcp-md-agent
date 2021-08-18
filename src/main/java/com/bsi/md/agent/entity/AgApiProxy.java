package com.bsi.md.agent.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 实时接口
 */
@Entity
@Data
@Table(name="md_agent_api_proxy")
public class AgApiProxy extends AgAbstractEntity {
    //所属配置id
    private Integer configId;
    //名称
    private String name;
}
