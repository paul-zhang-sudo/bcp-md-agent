package com.bsi.md.agent.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.schedule.FwTask;
import com.bsi.framework.core.utils.DateUtils;
import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.FwSpringContextUtil;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.datasource.AgApiTemplate;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.engine.factory.AgEngineFactory;
import com.bsi.md.agent.engine.integration.AgIntegrationEngine;
import com.bsi.md.agent.engine.integration.AgTaskBootStrap;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.AgJobParam;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.service.AgJobParamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.HashMap;
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
//        AgJobParamService agJobParamService = FwSpringContextUtil.getBean("agJobParamService", AgJobParamService.class);
//        AgJobParam param = getJobParam();
        try{
            //配置日志参数，不同日志输出到不同文件
            MDC.put("taskId", name+"-"+taskId);
            log.info("====计划任务开始执行,计划任务名称:{}，编码:{}====",name,taskId);
            //1、获取到执行规则
            AgIntegrationConfigVo config = JSON.parseObject( EHCacheUtil.getValue(AgConstant.AG_EHCACHE_JOB,taskId).toString(),AgIntegrationConfigVo.class);
            //2、调用集成引擎解析规则
            AgIntegrationEngine engine = AgEngineFactory.getJobEngine(config);
            Context context = new Context();
            context.setEnv(new HashMap());
            context.put("config", config.getParamMap());
            //处理输入、输出、转换节点的配置
            rebuildNode(config);
            context.put("inputConfig",config.getInputNode());
            context.put("outputConfig",config.getOutputNode());
            context.put("transformConfig",config.getTransformNode());

            AgTaskBootStrap.custom().context(context).engine(engine).exec();
        }catch (Exception e){
            result = "failure";
            error = ExceptionUtils.getFullStackTrace( e );
            log.error( "错误日志id:{},计划任务:{},执行失败,失败信息:{}", errorId , taskId ,error );
        }finally {
            log.info( "====计划任务:{},执行结束,执行结果:{}====", taskId , result );
            MDC.remove("taskId");
//            agJobParamService.save( param );
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


    private void rebuildNode(AgIntegrationConfigVo config){
        JSONObject in = config.getInputNode();
        JSONObject out = config.getOutputNode();
        JSONObject transform = config.getTransformNode();
        //删除无用配置
        in.remove("scriptContent");
        out.remove("scriptContent");
        transform.remove("scriptContent");
        //处理路径问题
        setRealpath(in);
        setRealpath(out);
    }

    private void setRealpath(JSONObject obj){
        AgApiTemplate a = AgDatasourceContainer.getApiDataSource(obj.getString("dataSource"));
        if(a!=null){
            obj.put("path",a.getApiUrl()+obj.getString("path"));
            obj.put("host",a.getApiUrl());
        }
    }
}
