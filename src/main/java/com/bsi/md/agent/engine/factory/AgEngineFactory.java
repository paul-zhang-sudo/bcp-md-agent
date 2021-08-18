package com.bsi.md.agent.engine.factory;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.integration.AgIntegrationEngine;
import com.bsi.md.agent.engine.integration.AgJobEngine;
import com.bsi.md.agent.engine.integration.input.AgInput;
import com.bsi.md.agent.engine.integration.output.AgOutput;
import com.bsi.md.agent.engine.integration.transform.AgTransform;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.entity.vo.AgNodeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

/**
 * 引擎工厂
 * @author fish
 */
@Slf4j
public class AgEngineFactory {
    /**
     * 获取定时执行引擎
     * @param config
     * @return
     */
    public static AgIntegrationEngine getJobEngine(AgIntegrationConfigVo config){
        AgJobEngine engine = new AgJobEngine();
        AgNodeVo inputNode = config.getInputNode();
        AgNodeVo outputNode = config.getOutputNode();
        AgNodeVo transformNode = config.getTransformNode();

        try{
            AgInput input = (AgInput) ClassUtils.getClass("com.bsi.md.agent.engine.integration.input."+inputNode.getClassName()).newInstance();
            input.setScript( inputNode.getScript() );

            AgTransform transform = (AgTransform) ClassUtils.getClass("com.bsi.md.agent.engine.integration.transform."+transformNode.getClassName()).newInstance();
            transform.setScript( transformNode.getScript() );

            AgOutput output = (AgOutput) ClassUtils.getClass("com.bsi.md.agent.engine.integration.output."+outputNode.getClassName()).newInstance();
            output.setScript( outputNode.getScript() );

            engine.setInput(input);
            engine.setTransform(transform);
            engine.setOutput(output);
        }catch (Exception e){
            log.error("创建集成引擎失败，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
            throw new RuntimeException(e);
        }

        return engine;
    }
}
