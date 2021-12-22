package com.bsi.md.agent.engine.integration;

/**
 * 集成引擎接口
 * @author fish
 */
public interface AgIntegrationEngine {
    /**
     * 输入
     * @param context
     * @return
     */
    Object input(Context context) throws Exception;

    /**
     * 转换
     * @param context
     * @return
     */
    Object transform(Context context) throws Exception;

    /**
     * 输出
     * @param context
     * @return
     */
    Object output(Context context) throws Exception;
}
