package com.bsi.md.agent.entity.dto;

import lombok.Data;

/**
 * 任务告警参数传输对象
 * @author fish
 */
@Data
public class AgTaskWarnConfDto {
    private String id;
    private String taskId;
    private Boolean enable;
    private String warnMethod;
}
