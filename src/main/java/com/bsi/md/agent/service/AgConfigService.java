package com.bsi.md.agent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.service.FwService;
import com.bsi.md.agent.entity.AgConfig;
import com.bsi.md.agent.entity.AgJob;
import com.bsi.md.agent.entity.dto.AgConfigDto;
import com.bsi.md.agent.repository.AgConfigRepository;
import com.bsi.md.agent.repository.AgJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 计划任务service
 */

@Service
@Transactional
public class AgConfigService extends FwService {
    @Autowired
    private AgConfigRepository agConfigRepository;

    @Autowired
    private AgJobService agJobService;

    @Autowired
    private AgJobRepository agJobRepository;

    /**
     * 查询所有集成配置
     * @return List<AgConfig>
     */
    public List<AgConfig> findAll(){
        return agConfigRepository.findAll();
    }

    /**
     * 更新配置
     */
    public void updateConfig(AgConfigDto config){
        //1、把配置数据保存到数据库
        //集成配置表
        JSONObject cnfObj = JSON.parseObject( config.getConfigValue() );
        AgConfig cf = new AgConfig();
        cf.setName( config.getName() );
        cf.setId( config.getId() );
        cf.setConfigValue( cnfObj.getJSONObject("config")==null?"":cnfObj.getJSONObject("config").toJSONString() );
        cf.setEnable( true );
        agConfigRepository.save(cf);

        //任务表
        JSONArray arr = cnfObj.getJSONArray("jobList");
        agJobRepository.deleteByConfigId( cf.getId() );
        for( int i=0;i<arr.size();i++ ){
            JSONObject obj = arr.getJSONObject(i);
            AgJob job = new AgJob();
            job.setId( obj.getString("jobId") );
            job.setConfigId( config.getId() );
            job.setName( obj.getString("jobName") );
            job.setEnable(true);
            JSONObject inputNode = obj.getJSONObject("inputNodeConfig");
            job.setCron(inputNode.getString("cron"));
            job.setConfigValue( obj.toJSONString() );
            agJobRepository.save(job);
        }

        //2、刷新缓存
        agJobService.refreshJob();
    }
}
