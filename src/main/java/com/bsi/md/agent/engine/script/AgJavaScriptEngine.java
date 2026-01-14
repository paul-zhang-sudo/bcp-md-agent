package com.bsi.md.agent.engine.script;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.script.*;

/**
 * js脚本引擎接口
 * @author fish
 */

public class AgJavaScriptEngine implements AgScriptEngine{

    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

//    private static AgJavaScriptEngine instance = null;

    private final ScriptEngine engine;

    /**
     * 返回单例
     *
     * @return
     */
//    public static AgJavaScriptEngine getInstance() {
//        if (instance == null)
//            instance = new AgJavaScriptEngine();
//        return instance;
//    }

    /**
     * 无参构造器 初始化需要的js引擎
     *
     */
    public AgJavaScriptEngine() {
        try {
            //调用Java8 nashorn 运行JavaScript脚本
            this.engine = new ScriptEngineManager().getEngineByName("nashorn");
            ScriptContext sc = new SimpleScriptContext();
            Bindings bindings = new SimpleBindings();
            bindings.put("log", info_log); // 向nashorn引擎注入logger对象
            sc.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
            sc.getBindings(ScriptContext.ENGINE_SCOPE).putAll(bindings);
            engine.setBindings(sc.getBindings(ScriptContext.ENGINE_SCOPE), ScriptContext.ENGINE_SCOPE);
            //支持importClass
            engine.eval("load('nashorn:mozilla_compat.js')");
        }catch (Exception e){
            info_log.error("javaScript引擎初始化失败:{}", ExceptionUtils.getFullStackTrace(e));
            throw new RuntimeException("js脚本初始化失败");
        }

    }
    /**
     * 执行方法
     * @param script
     * @return
     */
    public Object execute(String script,String method,Object[] args) throws Exception{
        Object result = eval(script);
        if( StringUtils.hasText(method) ){
            Invocable invocable = (Invocable) engine;
//            log.info("args:{}", JSON.toJSONString(args));
            result = invocable.invokeFunction(method,args);
        }
        return result;
    }

    public Object executeMethod(String method,Object[] args) throws Exception{
        Invocable invocable = (Invocable) engine;
        return invocable.invokeFunction(method,args);
    }

    public Object eval(String script) throws Exception{
        return engine.eval(script);
    }
}
