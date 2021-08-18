package com.bsi.md.agent.engine.script;

import com.alibaba.fastjson.JSON;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.script.*;

/**
 * js脚本引擎接口
 * @author fish
 */

@Slf4j
public class AgJavaScriptEngine implements AgScriptEngine{

    private static AgJavaScriptEngine instance = null;

    private ScriptEngine engine;

    /**
     * 返回单例
     *
     * @return
     */
    public static AgJavaScriptEngine getInstance() {
        if (instance == null)
            instance = new AgJavaScriptEngine();
        return instance;
    }

    /**
     * 无参构造器 初始化需要的js引擎
     *
     */
    private AgJavaScriptEngine() {
        try {
            //调用Java8 nashorn 运行JavaScript脚本
            this.engine = new ScriptEngineManager().getEngineByName("nashorn");
            ScriptContext sc = new SimpleScriptContext();
            Bindings bindings = new SimpleBindings();
            bindings.put("log", log); // 向nashorn引擎注入logger对象
            sc.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
            sc.getBindings(ScriptContext.ENGINE_SCOPE).putAll(bindings);
            engine.setBindings(sc.getBindings(ScriptContext.ENGINE_SCOPE), ScriptContext.ENGINE_SCOPE);
            //支持importClass
            engine.eval("load('nashorn:mozilla_compat.js')");
        }catch (Exception e){
            log.error("javaScript引擎初始化失败:{}", ExceptionUtils.getFullStackTrace(e));
            throw new RuntimeException("js脚本初始化失败");
        }

    }
    /**
     * 执行方法
     * @param script
     * @return
     */
    public Object execute(String script,String method) throws Exception{
        Object result = eval(script);
        if( StringUtils.hasText(method) ){
            Invocable invocable = (Invocable) engine;
            result = invocable.invokeFunction(method);
        }
        return result;
    }

    public Object eval(String script) throws Exception{
        return engine.eval(script);
    }

    public static void main(String[] arr) throws Exception{

        String script = "importClass(com.bsi.utils.DBUtils);\n" +
                "\n" +
                "function execute(){\n" +
                "   return DBUtils.execute(\"INSERT INTO price(materialCode,materialName,amount) VALUES('999','999',99.99);update price set materialName = '光11111' WHERE id=1;\");\n" +
                "}";
        System.out.println( JSON.toJSONString( AgJavaScriptEngine.getInstance().execute(script,"execute") ) );
    }
}
