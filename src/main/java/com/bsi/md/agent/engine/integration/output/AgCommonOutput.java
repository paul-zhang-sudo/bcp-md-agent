package com.bsi.md.agent.engine.integration.output;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.engine.script.AgJavaScriptEngine;
import com.bsi.md.agent.engine.script.AgScriptEngine;
import com.bsi.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用输出节点
 */
public class AgCommonOutput implements AgOutput{
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    //脚本
    protected String script;

    //执行引擎
    protected AgScriptEngine engine;

    @Override
    public Object write(Context context) throws Exception{
        Object result = null;
        try {
            result = engine.execute(script,"output",new Object[]{context,context.getData()});
        }catch (Exception e){
            info_log.error("写入数据报错:{}", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        return result;
    }

    @Override
    public String setScript(String script) {
        return this.script = script;
    }

    @Override
    public void setEngine(AgScriptEngine engine) {
        this.engine = engine;
    }
}
