package com.bsi.md.agent.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.httpclient.utils.IoTEdgeUtil;
import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.vo.resp.FwHttpStatus;
import com.bsi.framework.core.vo.resp.Resp;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.engine.factory.AgEngineFactory;
import com.bsi.md.agent.engine.integration.AgIntegrationEngine;
import com.bsi.md.agent.engine.integration.AgTaskBootStrap;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.dto.*;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.service.AgConfigService;
import com.bsi.md.agent.service.AgDataSourceService;
import com.bsi.md.agent.utils.AgConfigUtils;
import com.huaweicloud.sdk.iot.module.ItClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置修改api
 * @author fish
 */
@Slf4j
@RestController
@RequestMapping(value = "/api")
public class AgConfigController {

    @Autowired
    private AgDataSourceService agDataSourceService;
    @Autowired
    private AgConfigService agConfigService;


    /**
     * 更新集成配置
     * @param request
     * @param config
     * @throws Exception
     */
    @PostMapping("/iot/config")
    public Resp updateConfigForIot(HttpServletRequest request, @RequestBody AgConfigDto config) throws Exception{
        log.info( "收到bcp控制台通过iot通道下发的配置信息:{}", JSON.toJSONString( config ) );
        //IOT验签
        Resp rs = verify(request);
        if( FwHttpStatus.FORBIDDEN.value() == rs.getCode() ){
            return rs;
        }
        return updateConfig(config);
    }

    /**
     * 更新集成配置
     * @param request
     * @param config
     * @throws Exception
     */
    @PostMapping("/iot/integration/config")
    public Resp updateConfigForIot(HttpServletRequest request, @RequestBody AgConfigDtoForIOT config) throws Exception{
        log.info( "收到IoT Edge控制台下发的配置信息:{}", JSON.toJSONString( config ) );
        AgConfigDto c = transform(config);
        log.info("转换之后的配置信息:{}",JSON.toJSONString(c));
        //IOT验签
        Resp rs = verify(request);
        if( FwHttpStatus.FORBIDDEN.value() == rs.getCode() ){
            return rs;
        }
        return updateConfig(c);
    }

    /**
     * 更新数据源
     * @param request
     * @param dataSource
     * @throws Exception
     */
    @PostMapping("/iot/integration/datasource")
    public Resp updateDataSourceForIot(HttpServletRequest request,@RequestBody AgDataSourceDtoForIOT dataSource) throws Exception{
        log.info( "收到IoT Edge控制台下发的数据源信息:{}", JSON.toJSONString( dataSource ) );
        AgDataSourceDto ds = transform(dataSource);
        log.info("转换之后的数据源信息:{}",JSON.toJSONString(ds));
        //IOT验签
        Resp rs = verify(request);
        if( FwHttpStatus.FORBIDDEN.value() == rs.getCode() ){
            return rs;
        }
        return updateDataSource(ds);
    }

    /**
     * 把AgConfigDtoForIOT对象转换成AgConfigDto
     * @param config
     * @return AgConfigDto
     */
    private AgConfigDto transform(AgConfigDtoForIOT config){
        AgConfigDto dto = new AgConfigDto();
        dto.setId(config.getId());
        dto.setName(config.getName());
        //拼接configValue
        JSONObject configValue = new JSONObject();
        configValue.put("config",config.getConfig_values());
        JSONArray jobs = JSON.parseArray( config.getJobs() );
        JSONObject ds = JSON.parseObject(config.getData_sources());
        JSONArray arr = new JSONArray();
        for(int i=0;i<jobs.size();i++){
            JSONObject job = jobs.getJSONObject(i);
            JSONObject tmp = new JSONObject();
            tmp.put("jobId",job.getString("id"));
            tmp.put("enable",job.getBoolean("enabled"));
            tmp.put("jobName",job.getString("name"));
            //输入节点组转
            JSONObject input = job.getJSONObject("input");
            input.put("cron",job.getString("cron"));
            input.put("dataSource",ds.getString(input.getString("ds_key")));
            input.remove("ds_key");
            tmp.put("inputNodeConfig",input);
            //输出几点组转
            JSONObject output = job.getJSONObject("output");
            output.put("dataSource",ds.getString(output.getString("ds_key")));
            output.remove("ds_key");
            tmp.put("outputNodeConfig",output);
            //转换节点组转
            tmp.put("transformNodeConfig",job.getJSONObject("transform"));
            arr.add(tmp);
        }
        configValue.put("jobList",arr);
        dto.setConfigValue( JSON.toJSONString(configValue) );
        return dto;
    }

    /**
     * 把AgDataSourceDtoForIOT对象转换成AgDataSourceDto
     * @param ds
     * @return AgDataSourceDto
     */
    private AgDataSourceDto transform(AgDataSourceDtoForIOT ds){
        AgDataSourceDto dto = new AgDataSourceDto();
        dto.setId(ds.getId());

        dto.setDelFlag(ds.getDel_flag());
        //配置信息
        JSONObject cf = JSON.parseObject( ds.getConfig_values() );
        dto.setName(cf.getString("name"));
        dto.setType( cf.getString("type") );
        dto.setClassify(cf.getString("classify"));
        if(("db").equals(dto.getType())){
            cf.put("url",getDbUrl(cf.getString("classify"),cf));
        }
        dto.setConfigValue( cf.toJSONString() );
        return dto;
    }

    /**
     * 根据数据库类型生成连接字符串
     * @param dbType
     * @param obj
     * @return
     */
    private String getDbUrl(String dbType,JSONObject obj){
        String url="";
        if( "oracle".equals( dbType ) ){
            url ="dbc:oracle:thin:@//%s:%s/%s";
        }else if( "sqlserver".equals( dbType ) ){
            url = "jdbc:sqlserver://%s:%s;DatabaseName=%s";
        }
        return String.format(url,obj.getString("url"),obj.getString("port"),obj.getString("databaseName"));
    }

    /**
     * 更新集成配置
     * @aram request
     * @param config
     * @throws Exception
     */
    @PostMapping("/console/config")
    public Resp updateConfigForConsole(@RequestBody AgConfigDto config) throws Exception{
        log.info( "收到bcp控制台下发的配置信息:{}", JSON.toJSONString( config ) );
        return updateConfig(config);
    }

    /**
     * 更新数据源
     * @param request
     * @param dataSource
     * @throws Exception
     */
    @PostMapping("/iot/datasource")
    public Resp updateDataSourceForIot(HttpServletRequest request,@RequestBody AgDataSourceDto dataSource) throws Exception{
        log.info( "收到bcp控制台通过iot通道下发的数据源信息:{}", JSON.toJSONString( dataSource ) );
        //IOT验签
        Resp rs = verify(request);
        if( FwHttpStatus.FORBIDDEN.value() == rs.getCode() ){
            return rs;
        }
        return updateDataSource(dataSource);
    }

    /**
     * 更新数据源
     * @param dataSource
     * @throws Exception
     */
    @PostMapping("/console/datasource")
    public Resp updateDataSourceForConsole(@RequestBody AgDataSourceDto dataSource) throws Exception{
        log.info( "收到bcp控制台下发的数据源信息:{}", JSON.toJSONString( dataSource ) );
        return updateDataSource(dataSource);
    }

    /**
     * 补数、执行任务
     * @param param
     * @throws Exception
     */
    @PostMapping("/console/run-task")
    public Resp runTask(@RequestBody AgTaskParamDto param) throws Exception{
        log.info( "通过bcp控制台手动运行任务，参数:{}", JSON.toJSONString(param) );
        return repair(param);
    }

    /**
     * 补数、执行任务
     * @param param
     * @throws Exception
     */
    @PostMapping("/iot/run-task")
    public Resp runTaskForIot(HttpServletRequest request,@RequestBody AgTaskParamDto param) throws Exception{
        log.info( "通过iot edge手动运行任务，参数:{}", JSON.toJSONString(param) );
        //IOT验签
        Resp rs = verify(request);
        return repair(param);
    }

    private Resp repair(AgTaskParamDto param){
        Resp resp = new Resp();
        String error = "";
        String result = "success";
        try{
            //配置日志参数，不同日志输出到不同文件
            MDC.put("taskId", param.getTaskId()+"-repair");
            //1、获取到执行规则
            AgIntegrationConfigVo config = JSON.parseObject( EHCacheUtil.getValue(AgConstant.AG_EHCACHE_JOB,param.getTaskId()).toString(),AgIntegrationConfigVo.class);
            if(config==null){
                resp.setErrorCodeAndMsg(500,"前置机未找到taskId为{}的任务！");
                return resp;
            }
            log.info("====开始手动执行任务:{}====",param.getTaskId());


            //2、调用集成引擎解析规则
            AgIntegrationEngine engine = AgEngineFactory.getJobEngine(config);
            Context context = new Context();
            context.setEnv(new HashMap());
            //将补数据的参数设置到计划任务的上下文中去
            Map<String,Object> paramMap = config.getParamMap();
            paramMap.put("repairParam",param);
            context.put("config", paramMap);
            //处理输入、输出、转换节点的配置
            AgConfigUtils.rebuildNode(config);
            context.put("inputConfig",config.getInputNode());
            context.put("outputConfig",config.getOutputNode());
            context.put("transformConfig",config.getTransformNode());

            AgTaskBootStrap.custom().context(context).engine(engine).exec();
        }catch (Exception e){
            result = "failure";
            error = ExceptionUtils.getFullStackTrace( e );
            log.error( "计划任务:{},执行失败,失败信息:{}" , param.getTaskId() ,error );
        }finally {
            log.info( "====计划任务:{},执行结束,执行结果:{}====", param.getTaskId() , result );
            MDC.remove("taskId");
        }
        return resp;
    }

    /**
     * IOT验签
     * @param request
     * @return
     */
    private Resp verify(HttpServletRequest request){
        Resp resp = new Resp();
        //IOT验签
        try{
            IoTEdgeUtil.getItClient().verifyByDaemon( request.getHeader(ItClient.X_AUTHORIZATION) );
        }catch (Exception e){
            log.error("验签失败，错误信息:{}",ExceptionUtils.getFullStackTrace(e));
            resp.setCode( FwHttpStatus.FORBIDDEN.value() );
            resp.setMsg( "验证失败，不是来自iot的请求" );
        }
        return resp;
    }

    /**
     * 集成配置修改
     * @param config
     * @return
     */
    private Resp updateConfig(AgConfigDto config){
        Resp rs = new Resp();
        rs.setMsg("下发成功");
        try{
            agConfigService.updateConfig(config);
        }catch (Exception e){
            log.error("下发失败,错误信息：{}", ExceptionUtils.getFullStackTrace(e));
            rs.setCode(FwHttpStatus.INTERNAL_SERVER_ERROR.value());
            rs.setMsg("下发失败,错误信息:"+e.getMessage());
        }
        return rs;
    }

    private Resp updateDataSource(AgDataSourceDto dataSource){
        Resp rs = new Resp();
        rs.setMsg("下发成功");
        try{
            agDataSourceService.updateDS(dataSource);
        }catch (Exception e){
            log.error("下发失败,错误信息：{}", ExceptionUtils.getFullStackTrace(e));
            rs.setCode(FwHttpStatus.INTERNAL_SERVER_ERROR.value());
            rs.setMsg("下发失败,错误信息:"+e.getMessage());
        }
        return rs;
    }

}
