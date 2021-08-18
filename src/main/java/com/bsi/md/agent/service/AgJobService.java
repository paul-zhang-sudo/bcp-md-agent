package com.bsi.md.agent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.schedule.FwScheduleUtils;
import com.bsi.framework.core.service.FwService;
import com.bsi.framework.core.utils.CollectionUtils;
import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.entity.AgApiProxy;
import com.bsi.md.agent.entity.AgConfig;
import com.bsi.md.agent.entity.AgJob;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.entity.vo.AgNodeVo;
import com.bsi.md.agent.repository.AgJobRepository;
import com.bsi.md.agent.task.AgTaskRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class AgJobService extends FwService {
    @Autowired
    private AgJobRepository agJobRepository;
    @Autowired
    private AgApiProxyService agApiProxyService;
    @Autowired
    private AgConfigService agConfigService;

    /**
     * 查询系统中所有启用的定时任务
     *
     * @return
     */
    public List<AgJob> findAllEnable() {
        AgJob example = new AgJob();
        example.setEnable(true);
        return agJobRepository.findAll(Example.of(example));
    }

    public Boolean refreshJob() {
        Boolean flag=true;
        int jobSize = 0;
        int apiSize = 0;
        try{
            //1、读取系统中所有的数据源、集成配置、任务
            Map<Integer, AgConfig> agConfigMap = new HashMap<>();
            List<AgConfig> configList = agConfigService.findAll();
            if (CollectionUtils.isNotEmpty(configList)) {
                agConfigMap = configList.stream().collect(Collectors.toMap(AgConfig::getId, Function.identity()));
            }
            List<AgJob> jobList = this.findAllEnable();
            List<AgApiProxy> apiProxyList = agApiProxyService.findAllEnable();

            //3、定时任务初始化
            if (CollectionUtils.isNotEmpty(jobList)) {
                jobSize = jobList.size();
                List<AgTaskRun> taskList = new ArrayList<>();
                for (AgJob job : jobList) {
                    //配置初始化到缓存中
                    AgIntegrationConfigVo vo = new AgIntegrationConfigVo();
                    AgConfig agConfig = agConfigMap.get(job.getConfigId());
                    vo.setTaskId(job.getId());
                    vo.setConfigId(job.getConfigId());
                    JSONObject conf = JSONObject.parseObject(job.getConfigValue());
                    JSONObject inputNodeConfig = conf.getJSONObject("inputNodeConfig");
                    JSONObject transformNodeConfig = conf.getJSONObject("transformNodeConfig");
                    JSONObject outputNodeConfig = conf.getJSONObject("outputNodeConfig");

                    JSONObject globalConf = JSONObject.parseObject( agConfig.getConfigValue() );
                    JSONObject configParam = globalConf.getJSONObject("config");

                    AgNodeVo inputNode = getAgNodeVo(inputNodeConfig);
                    AgNodeVo outputNode = getAgNodeVo(outputNodeConfig);
                    AgNodeVo transformNode = getAgNodeVo(transformNodeConfig);

                    vo.setInputNode(inputNode);
                    vo.setOutputNode(outputNode);
                    vo.setTransformNode(transformNode);
                    vo.setParamMap(configParam.getInnerMap());

                    //初始化配置到缓存
                    EHCacheUtil.put( job.getId().toString(), JSON.toJSONString(vo) );
                    //初始化计划任务
                    AgTaskRun agTaskRun = new AgTaskRun();
                    agTaskRun.setCron(job.getCron());
                    agTaskRun.setTaskId(job.getId().toString());
                    agTaskRun.setName(job.getName());
                    taskList.add(agTaskRun);


                }
                FwScheduleUtils.addTasks(taskList);
                FwScheduleUtils.refreshTasks();
            }

            //4、实时接口初始化 TODO
            if (CollectionUtils.isNotEmpty(apiProxyList)) {

            }
        }catch (Exception e){
            log.error("刷新计划任务缓存失败:{}"+ ExceptionUtils.getFullStackTrace(e));
            flag=false;
        }
        log.info("一共初始化{}条定时任务,{}个实时接口",jobSize,apiSize);
        return flag;
    }

    private AgNodeVo getAgNodeVo(JSONObject node){
        return AgNodeVo.builder().type( node.getString("type") )
                .classify( node.getString("classify") )
                .className( node.getString("className") )
                .script( node.getString("scriptContent") )
                .dataSourceId( node.getString("dataSourceId") ).build();
    }
}
