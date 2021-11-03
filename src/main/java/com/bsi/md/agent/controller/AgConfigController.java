package com.bsi.md.agent.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.httpclient.utils.IoTEdgeUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.vo.resp.FwHttpStatus;
import com.bsi.framework.core.vo.resp.Resp;
import com.bsi.md.agent.entity.dto.AgConfigDto;
import com.bsi.md.agent.entity.dto.AgConfigDtoForIOT;
import com.bsi.md.agent.entity.dto.AgDataSourceDto;
import com.bsi.md.agent.entity.dto.AgDataSourceDtoForIOT;
import com.bsi.md.agent.service.AgConfigService;
import com.bsi.md.agent.service.AgDataSourceService;
import com.huaweicloud.sdk.iot.module.ItClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
//        IOT验签
//        Resp rs = verify(request);
//        if( FwHttpStatus.FORBIDDEN.value() == rs.getCode() ){
//            return rs;
//        }
//        return updateConfig(c);
        Resp res = new Resp();
        res.setMsg("下发成功");
        return res;
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
//        Resp rs = verify(request);
//        if( FwHttpStatus.FORBIDDEN.value() == rs.getCode() ){
//            return rs;
//        }
//        return updateDataSource(ds);
        Resp res = new Resp();
        res.setMsg("下发成功");
        return res;
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
        dto.setName(ds.getName());
        dto.setDelFlag(ds.getDel_flag());
        JSONObject obj = new JSONObject();
        //配置信息
        JSONObject cf = JSON.parseObject( ds.getConfig_values() );
        dto.setType( cf.getString("type") );
        dto.setConfigValue( ds.getConfig_values() );
        return dto;
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
