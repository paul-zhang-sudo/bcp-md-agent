package com.bsi.md.agent.engine.plugins;

import com.bsi.md.agent.engine.integration.Context;

/**
 * 输出之后调用的插件
 */
public interface AgAfterOutputPlugin {
    void handlerMsg(Context context);
}
