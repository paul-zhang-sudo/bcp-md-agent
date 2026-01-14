package com.bsi.md.agent.engine.pool;


import com.bsi.md.agent.engine.script.AgScriptEngine;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fish
 * 执行引擎池
 */
public class AgExecEnginePool {
    private static final Map<String, AgScriptEngine> engineMapMap = new HashMap<>();

    /**
     * 添加一个执行引擎
     * @param key
     * @param engine
     */
    public static void addEngine(String key, AgScriptEngine engine){
        engineMapMap.put(key,engine);
    }

    /**
     * 获取一个执行引擎
     * @param key
     * @param engine
     */
    public static AgScriptEngine getEngine(String key){
        return engineMapMap.get(key);
    }

    /**
     * 清空jdbc数据源map
     */
    public static void clearEngine(){
        engineMapMap.clear();
    }
}
