package com.bsi.md.agent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bsi.framework.core.service.FwService;
import com.bsi.framework.core.utils.CollectionUtils;
import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.entity.AgJob;
import com.bsi.md.agent.entity.AgJobConfig;
import com.bsi.md.agent.entity.AgWarnMethod;
import com.bsi.md.agent.entity.dto.AgTaskWarnConfDto;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.repository.AgJobConfigRepository;
import com.bsi.md.agent.repository.AgJobRepository;
import com.bsi.md.agent.repository.AgWarnMethodRepository;
import com.bsi.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class AgWarnMethodService extends FwService {
    @Autowired
    private AgWarnMethodRepository agWarnMethodRepository;
    @Autowired
    private AgJobRepository agJobRepository;
    @Autowired
    private AgJobConfigRepository agJobConfigRepository;

    /**
     * 查询所有数据源配置
     * @return List<AgDataSource>
     */
    public List<AgWarnMethod> findAll(){
        return agWarnMethodRepository.findAll();
    }

    public boolean refreshWarnMethod(){
        boolean flag = true;
        int size = 0;
        try {
            List<AgWarnMethod> list = findAll();
            //清空告警配置
            EHCacheUtil.removeAllEhcache(AgConstant.AG_EHCACHE_WARN);

            if( CollectionUtils.isNotEmpty(list) ) {
                size = list.size();
                for (AgWarnMethod agWarnMethod : list) {
                    //启用的才初始化
                    if( agWarnMethod.getEnable() ){
                        EHCacheUtil.setValue(AgConstant.AG_EHCACHE_WARN,agWarnMethod.getId(), JSONUtils.toJson(agWarnMethod));
                    }
                }
            }
        }catch (Exception e){
            log.error("刷新告警配置报错:{}", ExceptionUtils.getFullStackTrace(e));
            flag = false;
        }
        log.info("一共初始化{}条告警配置信息",size);
        return flag;
    }

    /**
     * 更新告警方法
     * @param warnConf
     * @param config
     */
    public void updateTaskAndMethod(AgTaskWarnConfDto warnConf,AgIntegrationConfigVo config){
        //保存或者修改告警方式
        AgWarnMethod m = JSON.parseArray(warnConf.getWarnMethod(),AgWarnMethod.class).get(0);
        agWarnMethodRepository.save(m);
        //String methodIds = mlist.stream().map(a->a.getId()).collect(Collectors.joining(","));

        //保存或者修改任务的告警配置
        AgJobConfig jc = new AgJobConfig();
        jc.setId(warnConf.getId());
        jc.setTaskId(warnConf.getTaskId());
        jc.setEnable(warnConf.getEnable());
        jc.setWarnMethodId(m.getId());
        agJobConfigRepository.save(jc);

        //更新任务配置缓存，加上告警的配置
        config.setWarnMethodId(warnConf.getEnable()?m.getId():"");
        EHCacheUtil.setValue(AgConstant.AG_EHCACHE_JOB,warnConf.getTaskId(),JSON.toJSONString(config));

        //刷新告警缓存
        refreshWarnMethod();
    }
}
