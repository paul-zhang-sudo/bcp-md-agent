package com.bsi.md.agent.engine.integration.output;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.engine.script.AgJavaScriptEngine;
import com.bsi.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用输出节点
 */
@Slf4j
public class AgCommonOutput implements AgOutput{
    //脚本
    protected String script;

    @Override
    public Object write(Context context) throws Exception{
        Object result = null;
        try {
            result = AgJavaScriptEngine.getInstance().execute(script,"output",new Object[]{context,context.getData()});
        }catch (Exception e){
            log.error("写入数据报错:{}", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        return result;
    }

    @Override
    public String setScript(String script) {
        return this.script = script;
    }
}
