package com.bsi.utils;

import com.bsi.md.agent.engine.pool.AgAsynchronousApiPool;
import com.bsi.md.agent.task.AgTaskRun;

/**
 * @author fish
 * 异步执行任务工具类
 */
public class AsyncTaskUtils {
    /**
     * 异步执行指定任务
     * @param taskId 任务id
     * @param taskName 任务名称
     * @param param 执行参数
     */
    public static void exec(String taskId,String taskName,String param){
        AgTaskRun task = new AgTaskRun();
        task.setTaskId(taskId);
        task.setName(taskName);
        task.setParam(param);
        AgAsynchronousApiPool.commit(task);
    }
}
