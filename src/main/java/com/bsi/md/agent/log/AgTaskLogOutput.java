package com.bsi.md.agent.log;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计划任务日志输出
 */
@Slf4j
public class AgTaskLogOutput {

    private static Logger task_log = LoggerFactory.getLogger("TASK_RUN_LOG");


    public static void outputLog(AgTaskLog taskLog){
        //示例 103.3.77.203 oa_org_hr oa推送组织到hr [2020-11-11 00:00:00] 0.000 sucess 10 8 -
        task_log.info(
                String.format("%s %s %s [%s] %s %s %s %s %s %s",
                        taskLog.getClientIp(),
                        taskLog.getTaskCode(),
                        taskLog.getTaskName(),
                        taskLog.getTimeLocal(),
                        taskLog.getExecTime(),
                        taskLog.getResult(),
                        taskLog.getValidSize(),
                        taskLog.getSuccessSize(),
                        (taskLog.getValidSize()-taskLog.getSuccessSize()),
                        taskLog.getErrorId()
                )
        );
    }
}
