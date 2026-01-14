package com.bsi.md.agent.engine.plugins;

import com.alibaba.fastjson.JSON;
import com.bsi.framework.core.utils.FwSpringContextUtil;
import com.bsi.md.agent.engine.integration.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 插件管理器
 */
public class AgAfterOutputPluginManager {
    //插件列表
    private static final Map<String,AgAfterOutputPlugin> pluginsMap;

    static {
        pluginsMap =  FwSpringContextUtil.getApplicationContext().getBeansOfType(AgAfterOutputPlugin.class);
    }

    /**
     * 运行插件
     */
    public static void runPlugins(Context context){
        pluginsMap.forEach((k,v)->v.handlerMsg(context));
    }
}
