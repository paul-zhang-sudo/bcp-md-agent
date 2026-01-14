package com.bsi.md.agent.engine.integration.input;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.engine.script.AgJavaScriptEngine;
import com.bsi.md.agent.engine.script.AgScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用输入节点（脚本）
 */
public class AgCommonInput implements AgInput{
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    //脚本
    protected String script;
    //执行引擎
    protected AgScriptEngine engine;

    @Override
    public Object read(Context context) throws Exception{
        Object result = null;
        try {
            result = engine.execute(script,"input",new Object[]{context});
        }catch (Exception e){
            info_log.error("查询数据报错:{}", ExceptionUtils.getFullStackTrace(e));
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
