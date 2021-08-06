package com.bsi.md.agent.entity;

import com.bsi.framework.core.entity.AbstractPageEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 任务参数
 */
@Entity
@Data
@Table(name="md_agent_job_param")
public class AgJobParam extends AbstractPageEntity {
    //系统名称
    private String name;
    //任务id
    private Long jobId;
    //最后执行时间
    private Date lastRunTime;
    //最后执行标志
    private String lastRunFlag;
}
