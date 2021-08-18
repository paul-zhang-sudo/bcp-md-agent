package com.bsi.md.agent.engine.integration.input;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.engine.script.AgJavaScriptEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用输入节点（脚本）
 */
@Slf4j
public class AgCommonInput implements AgInput{

    //脚本
    protected String script;

    @Override
    public Object read(Context context) {
        Object result = null;
        try {
            result = AgJavaScriptEngine.getInstance().execute(script,"input");
        }catch (Exception e){
            log.error("写入数据报错:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return result;
    }

    @Override
    public String setScript(String script) {
        return this.script = script;
    }
}
