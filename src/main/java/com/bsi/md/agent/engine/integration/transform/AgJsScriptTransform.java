package com.bsi.md.agent.engine.integration.transform;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.engine.script.AgJavaScriptEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * js脚本转换
 * @author fish
 */
@Slf4j
public class AgJsScriptTransform implements AgTransform{
    //脚本
    protected String script;
    /**
     * 数据转换
     * @param context
     * @return
     */
    public Object transform(Context context) throws Exception{
        Object result = null;
        try {
            result = AgJavaScriptEngine.getInstance().execute(script,"transform",new Object[]{context,context.getData()});
        }catch (Exception e){
            log.error("转换数据报错:{}", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        return result;
    }

    @Override
    public String setScript(String script) {
        return this.script = script;
    }
}
