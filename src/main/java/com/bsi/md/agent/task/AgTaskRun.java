package com.bsi.md.agent.task;

import com.alibaba.fastjson.JSON;
import com.bsi.framework.core.schedule.FwTask;
import com.bsi.framework.core.utils.*;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.engine.factory.AgEngineFactory;
import com.bsi.md.agent.engine.integration.AgIntegrationEngine;
import com.bsi.md.agent.engine.integration.AgTaskBootStrap;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.AgJobParam;
import com.bsi.md.agent.entity.dto.AgTaskParamDto;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.log.AgTaskLog;
import com.bsi.md.agent.log.AgTaskLogOutput;
import com.bsi.md.agent.service.AgJobParamService;
import com.bsi.md.agent.utils.AgConfigUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.UUID;

/**
 * 计划任务类
 * @author fish
 */
@Data
public class AgTaskRun extends FwTask {
    private String taskId;
    private String name;
    private String param;
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    @Override
    public void run() {
        String result = "success";
        //初始化日志记录实体类
        String errorId = UUID.randomUUID().toString().replaceAll("-","");
        String error;
        long startTime = System.currentTimeMillis();
        AgTaskLog agTaskLog = AgTaskLog.builder().taskName(name).traceId(errorId).execType("自动").taskCode(taskId).errorId("-").timeLocal( DateUtils.toString( DateUtils.now() ) ).build();
        agTaskLog.setStartTime(com.bsi.utils.DateUtils.getDateStrFromTime(startTime, "yyyy-MM-dd HH:mm:ss.SSS"));
        try{
            //配置日志参数，不同日志输出到不同文件
            MDC.put("taskId", name+"-"+taskId);
            MDC.put("traceId", errorId);
            info_log.info("====计划任务开始执行,计划任务名称:{}，编码:{}====",name,taskId);
            //1、获取到执行规则
            Object taskConfig = EHCacheUtil.getValue(AgConstant.AG_EHCACHE_JOB,taskId);
            if(taskConfig==null){
                info_log.error("任务不存在，请检查");
                return;
            }
            AgIntegrationConfigVo config = JSON.parseObject( taskConfig.toString(),AgIntegrationConfigVo.class);
            //2、调用集成引擎解析规则
            AgIntegrationEngine engine = AgEngineFactory.getJobEngine(config);
            Context context = new Context();
            context.setEnv(new HashMap());
            //如果走的是异步任务
            if(StringUtils.hasText(param)){
                AgTaskParamDto dto = new AgTaskParamDto();
                dto.setRunParams(param);
                dto.setTaskId(taskId);
                config.getParamMap().put("repairParam",dto);
            }
            context.put("config", config.getParamMap());
            //处理输入、输出、转换节点的配置
            AgConfigUtils.rebuildNode(config);
            context.put("inputConfig",config.getInputNode());
            context.put("outputConfig",config.getOutputNode());
            context.put("transformConfig",config.getTransformNode());
            context.put("taskInfoLog",agTaskLog);
            context.put("ctx_task_id",taskId);
            context.put("ctx_warn_method_id",config.getWarnMethodId());

            AgTaskBootStrap.custom().context(context).engine(engine).exec();
        }catch (Exception e){
            result = "failure";
            error = ExceptionUtils.getFullStackTrace( e );
            agTaskLog.setErrorId(errorId);
            info_log.error( "错误日志id:{},计划任务:{},执行失败,失败信息:{}", errorId , taskId ,error );
        }finally {
            long endTime = System.currentTimeMillis();
            agTaskLog.setEndTime(com.bsi.utils.DateUtils.getDateStrFromTime(endTime, "yyyy-MM-dd HH:mm:ss.SSS"));
            agTaskLog.setExecTime( String.valueOf( (endTime-startTime)/1000d ) );
            agTaskLog.setResult( result );
            AgTaskLogOutput.outputLog(agTaskLog);
            info_log.info( "====计划任务:{},执行结束,执行结果:{}====", taskId , result );
            MDC.remove("taskId");
            MDC.remove("traceId");
        }

    }

    /**
     * 获取任务参数
     * @return AgJobParam
     */
    private AgJobParam getJobParam(){
        AgJobParam param = FwSpringContextUtil.getBean("agJobParamService", AgJobParamService.class).getByJobId(Integer.parseInt(taskId));
        if( param == null ){
            param = new AgJobParam();
            param.setJobId(Integer.parseInt(taskId));
            param.setLastRunTime(DateUtils.now());
        }
        return param;
    }

}
