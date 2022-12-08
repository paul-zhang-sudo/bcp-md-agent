package com.bsi.md.agent.proxy;//package com.bsi.framework.core.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.StringUtils;
import com.bsi.framework.core.utils.TokenUtils;
import com.bsi.framework.core.vo.resp.FwHttpStatus;
import com.bsi.framework.core.vo.resp.Resp;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.engine.factory.AgEngineFactory;
import com.bsi.md.agent.engine.integration.AgIntegrationEngine;
import com.bsi.md.agent.engine.integration.AgTaskBootStrap;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.utils.AgApiProxyUtils;
import com.bsi.md.agent.utils.AgConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 代理api
 * @author fish
 */
@Slf4j
@WebFilter(filterName="agApiProxyFilter",urlPatterns="/*")
public class AgApiProxyFilter implements Filter {
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        response.setCharacterEncoding("utf-8");
        AgIntegrationConfigVo config = AgApiProxyUtils.isProxied(request.getRequestURI());
        if ( config==null ) {
            chain.doFilter(req, res);
            return;
        }
        MDC.put("taskId", config.getTaskName()+"-"+config.getTaskId());
        info_log.info("====开始调用{}====",config.getTaskName());
        JSONObject inputNode = config.getInputNode();
        //如果接口需要登录鉴权,则进行验证
        if( "y".equals( inputNode.getString("authFlag") ) ){
            String token = request.getHeader( AgConstant.AG_AUTHORIZATION );
            Resp resp = verity(token);
            if( FwHttpStatus.OK.value() != resp.getCode() ){
                response.setStatus(resp.getCode());
                response.getWriter().write( JSON.toJSONString(resp) );
                response.getWriter().close();
                info_log.info("接口鉴权失败");
                return;
            }
        }

        //登录鉴权通过
        try {
            //2、调用集成引擎解析规则
            AgIntegrationEngine engine = AgEngineFactory.getJobEngine(config);
            Context context = new Context();
            context.setEnv(new HashMap());
            //将补数据的参数设置到计划任务的上下文中去
            Map<String,Object> paramMap = config.getParamMap();
            context.put("config", paramMap);
            //添加api标识为y，如果是api输入节点未查询到数据不会终止执行
            context.put("api-flag","Y");
            //处理输入、输出、转换节点的配置
            AgConfigUtils.rebuildNode(config);
            context.put("inputConfig",config.getInputNode());
            context.put("outputConfig",config.getOutputNode());
            context.put("transformConfig",config.getTransformNode());
            Object obj = AgTaskBootStrap.custom().context(context).engine(engine).exec();
            //设置body
            if(obj!=null){
                if(obj instanceof byte[]){
                    OutputStream outputStream = response.getOutputStream();
                    IOUtils.write((byte[])obj,outputStream);
                    outputStream.flush();
                    outputStream.close();
                }else {
                    response.getWriter().write( obj.toString());
                    response.getWriter().close();
                }
            }
        }catch(Exception e){
            String errorId = UUID.randomUUID().toString();
            response.setStatus(FwHttpStatus.INTERNAL_SERVER_ERROR.value());
            Resp resp = new Resp();
            resp.setErrorCodeAndMsg(FwHttpStatus.INTERNAL_SERVER_ERROR.value(),"接口异常,异常id:"+errorId);
            response.getWriter().write( JSON.toJSONString(resp) );
            response.getWriter().close();
            info_log.error("代理api接口发生错误,errorId:{},错误信息:{}",errorId,ExceptionUtils.getFullStackTrace(e));
        }finally {
            info_log.info("===={}调用完毕====",config.getTaskName());
            MDC.remove("taskId");
        }


    }

    private Resp verity(String token){
        Resp resp = new Resp();
        resp.setCode( FwHttpStatus.FORBIDDEN.value() );
        if( StringUtils.isNullOrBlank( token ) ){
            resp.setMsg( "Incorrect token authentication information: Authorization header is missing" );
        }else if( !TokenUtils.verity(token) ){
            resp.setMsg("Incorrect token authentication information: Token is invalid");
        }else {
            resp.setCode( FwHttpStatus.OK.value() );
        }

        return resp;
    }

    @Override
    public void destroy() {
    }
}
