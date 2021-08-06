package com.bsi.md.agent.entity;

import com.bsi.framework.core.entity.AbstractPageEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * api仓库
 */
@Entity
@Data
@Table(name="md_agent_job")
public class AgJob extends AbstractPageEntity {
    //所属配置id
    private Long configId;
    //名称
    private String name;
    //cron表达式
    private String cron;
}
