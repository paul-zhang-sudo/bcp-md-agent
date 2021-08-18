package com.bsi.md.agent.controller;

import com.bsi.framework.core.httpclient.utils.IoTEdgeUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.vo.resp.FwHttpStatus;
import com.bsi.framework.core.vo.resp.Resp;
import com.bsi.md.agent.entity.dto.AgConfigDto;
import com.bsi.md.agent.entity.dto.AgDataSourceDto;
import com.bsi.md.agent.service.AgConfigService;
import com.bsi.md.agent.service.AgDataSourceService;
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
@RequestMapping(value = "/conf")
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
        //IOT验签
        Resp rs = verify(request);
        if( FwHttpStatus.FORBIDDEN.value() == rs.getCode() ){
            return rs;
        }
        return updateConfig(config);
    }

    /**
     * 更新集成配置
     * @aram request
     * @param config
     * @throws Exception
     */
    @PostMapping("/console/config")
    public Resp updateConfigForConsole(@RequestBody AgConfigDto config) throws Exception{
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
            IoTEdgeUtil.getItClient().verify(request.getHeader("Authorization"));
        }catch (Exception e){
            log.error("验签失败，错误信息:{}",ExceptionUtils.getFullStackTrace(e));
            resp.setCode( FwHttpStatus.FORBIDDEN.value() );
            resp.setMsg( "验证失败" );
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
