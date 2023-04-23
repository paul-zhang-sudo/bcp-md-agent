package com.bsi.md.agent.engine.factory;

import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.integration.AgIntegrationEngine;
import com.bsi.md.agent.engine.integration.AgJobEngine;
import com.bsi.md.agent.engine.integration.input.AgInput;
import com.bsi.md.agent.engine.integration.output.AgOutput;
import com.bsi.md.agent.engine.integration.transform.AgTransform;
import com.bsi.md.agent.engine.pool.AgExecEnginePool;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 引擎工厂
 * @author fish
 */
public class AgEngineFactory {
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    /**
     * 获取定时执行引擎
     * @param config
     * @return
     */
    public static AgIntegrationEngine getJobEngine(AgIntegrationConfigVo config){
        AgJobEngine engine = new AgJobEngine();
        JSONObject inputNode = config.getInputNode();
        JSONObject outputNode = config.getOutputNode();
        JSONObject transformNode = config.getTransformNode();

        try{
            AgInput input = (AgInput) ClassUtils.getClass("com.bsi.md.agent.engine.integration.input."+inputNode.getOrDefault("className","AgCommonInput")).newInstance();
            input.setScript( inputNode.getString("scriptContent") );
            input.setEngine( AgExecEnginePool.getEngine(config.getTaskId()) );

            AgTransform transform = (AgTransform) ClassUtils.getClass("com.bsi.md.agent.engine.integration.transform."+transformNode.getOrDefault("className","AgJsScriptTransform")).newInstance();
            transform.setScript( transformNode.getString("scriptContent") );
            transform.setEngine( AgExecEnginePool.getEngine(config.getTaskId()) );

            AgOutput output = (AgOutput) ClassUtils.getClass("com.bsi.md.agent.engine.integration.output."+outputNode.getOrDefault("className","AgCommonOutput")).newInstance();
            output.setScript( outputNode.getString("scriptContent") );
            output.setEngine( AgExecEnginePool.getEngine(config.getTaskId()) );

            engine.setInput(input);
            engine.setTransform(transform);
            engine.setOutput(output);
        }catch (Exception e){
            info_log.error("创建集成引擎失败，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
            throw new RuntimeException(e);
        }

        return engine;
    }
}
