package com.bsi.md.agent.task;

import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.schedule.FwTask;
import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.factory.AgEngineFactory;
import com.bsi.md.agent.engine.integration.AgIntegrationEngine;
import com.bsi.md.agent.engine.integration.AgTaskBootStrap;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 计划任务类
 * @author fish
 */
@Data
@Slf4j
public class AgTaskRun extends FwTask {
    private String taskId;
    private String name;

    @Override
    public void run() {
        String result = "success";
        //初始化日志记录实体类
        String errorId = UUID.randomUUID().toString().replaceAll("-","");
        String error;
        try{
            //配置日志参数，不同日志输出到不同文件
            MDC.put("taskId", taskId);
            log.info("====计划任务开始执行,计划任务名称:{}，编码:{}====",name,taskId);
            //1、获取到执行规则
            AgIntegrationConfigVo config = EHCacheUtil.get(taskId,AgIntegrationConfigVo.class);
            //2、调用集成引擎解析规则
            AgIntegrationEngine engine = AgEngineFactory.getJobEngine(config);
            Context context = new Context();
            context.setMap(new JSONObject());
            context.put("config",config);

            AgTaskBootStrap.custom().context(context).engine(engine).exec();
        }catch (Exception e){
            result = "failure";
            error = ExceptionUtils.getFullStackTrace( e );
            log.error( "错误日志id:{},计划任务:{},执行失败,日志id:{},失败信息:{}", errorId , taskId ,error );
        }finally {
            log.info( "====计划任务:{},执行结束,执行结果:{}====", taskId , result );
            MDC.remove("taskId");
        }

    }
}
