package com.bsi.md.agent.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 告警方式
 */
@Entity
@Data
@Table(name="md_agent_job_warn")
public class AgJobConfig extends AgAbstractEntity {
    //任务
    private String taskId;
    //告警方式
    private String warnMethodId;
    //状态:禁用启用
    private Boolean enable;
}
