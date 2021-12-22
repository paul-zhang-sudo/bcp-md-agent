package com.bsi.md.agent.engine.integration.input;

import com.bsi.md.agent.engine.integration.Context;

/**
 * 输入接口
 * @author fish
 */
public interface AgInput {
    /**
     * 查询数据
     * @param context
     * @return
     */
    Object read(Context context) throws Exception;

    /**
     * 设置执行脚本
     * @param script
     * @return
     */
    String setScript(String script);
}
