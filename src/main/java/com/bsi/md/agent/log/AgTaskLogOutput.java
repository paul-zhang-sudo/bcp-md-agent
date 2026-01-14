package com.bsi.md.agent.log;


import com.alibaba.fastjson.JSONArray;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.config.MongoConfigProperties;
import com.bsi.utils.MongoDBUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计划任务日志输出
 */
@Slf4j
public class AgTaskLogOutput {

    private static final Logger task_log = LoggerFactory.getLogger("TASK_RUN_LOG");


    public static void outputLog(AgTaskLog taskLog){
        try {
            //1、输出到任务日志中
            //示例 103.3.77.203 oa_org_hr oa推送组织到hr [2020-11-11 00:00:00] [2020-11-11 00:00:00] 1 sucess 10 8 -
            task_log.info(
                    String.format("%s %s %s %s %s [%s] [%s] %s %s %s %s %s %s",
                            taskLog.getClientIp(),
                            taskLog.getTraceId(),
                            taskLog.getTaskCode(),
                            taskLog.getTaskName(),
                            taskLog.getExecType(),
                            taskLog.getStartTime(),
                            taskLog.getEndTime(),
                            taskLog.getExecTime(),
                            taskLog.getResult(),
                            taskLog.getValidSize(),
                            taskLog.getSuccessSize(),
                            (taskLog.getValidSize()-taskLog.getSuccessSize()),
                            taskLog.getErrorId()
                    )
            );
            JSONArray arr = new JSONArray();
            arr.add(taskLog);
            //启动mongodb才输出日志到mongodb
            if(MongoConfigProperties.isEnabled()){
                MongoDBUtils.batchInsert(arr.toJSONString(),"task_run_log");
            }
        }catch (Exception e){
            log.error("任务运行日志输出异常:{}", ExceptionUtils.getFullStackTrace(e));
        }
    }
}
