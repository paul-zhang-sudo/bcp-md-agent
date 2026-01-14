package com.bsi.md.agent.engine.integration;

import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.framework.core.utils.StringUtils;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.engine.plugins.AgAfterOutputPluginManager;
import com.bsi.md.agent.log.AgTaskLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 集成引擎执行引导类
 * @author fish
 */
public class AgTaskBootStrap {

    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    private AgIntegrationEngine engine;

    private Context context;

    /**
     * 设置上下文
     * @param context
     * @return
     */
    public AgTaskBootStrap context(Context context){
        this.context = context;
        return this;
    }

    /**
     * 设置集成引擎
     * @param engine
     * @return
     */
    public AgTaskBootStrap engine(AgIntegrationEngine engine){
        this.engine = engine;
        return this;
    }
    /**
     * 初始化 AgTaskBootStrap
     * @return
     */
    public static AgTaskBootStrap custom(){
        return new AgTaskBootStrap();
    }

    /**
     * 执行
     */
    public Object exec() throws Exception{
        info_log.debug("1.执行输入节点");
        boolean apiFlag = context.get("api-flag") != null;
        boolean repairFlag = context.get("repair-flag") != null;
        //当前时间的毫秒数
        String ts = System.currentTimeMillis()+"";
        //上次运行成功的毫秒数
        String lastTs = (String) EHCacheUtil.getValue("task_last_success_time",context.getTaskId());
        info_log.info("lastTs:{}",lastTs);
        //如果任务是第一次运行，则设置上次成功运行的时间为当前时间
        if( !apiFlag && !repairFlag && StringUtils.isNullOrBlank(lastTs) ){
            EHCacheUtil.setValue("task_last_success_time",context.getTaskId(),ts,true);
        }
        context.put("ctx_task_last_success_ts", StringUtils.hasText(lastTs)?lastTs:ts);
        Object obj = engine.input(context);
        context.put(AgConstant.AG_DATA,obj);

        if(obj==null && !apiFlag){
            info_log.info("输入节点未查询到数据，结束任务");
            return null;
        }
        info_log.debug("2.执行转换节点");
        obj = engine.transform(context);
        context.put(AgConstant.AG_DATA,obj);
        info_log.debug("3.执行输出节点");
        Object result = engine.output(context);
        info_log.debug("4.设置成功处理数据时间");
        //如果不是api上报、并且不是补数、并且有效数量大于0条则设置成功时间
        boolean successFlag = context.getResultLog() != null && context.getResultLog().getValidSize() > 0;
        if( !apiFlag && !repairFlag && successFlag ){
            //如果手动设置了lastts，则取手动设置的值
            AgTaskLog taskLog = context.getResultLog();
            ts = StringUtils.hasText(taskLog.getLastTs())?taskLog.getLastTs():ts;
            EHCacheUtil.setValue("task_last_success_time",context.getTaskId(),ts,true);
        }
        info_log.debug("5.执行输出完成之后的插件");
        AgAfterOutputPluginManager.runPlugins(context);
        info_log.debug("执行完毕");
        return result;
    }
}
