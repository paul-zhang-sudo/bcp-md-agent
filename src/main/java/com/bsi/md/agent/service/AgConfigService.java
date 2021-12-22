package com.bsi.md.agent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.service.FwService;
import com.bsi.md.agent.entity.AgApiProxy;
import com.bsi.md.agent.entity.AgConfig;
import com.bsi.md.agent.entity.AgJob;
import com.bsi.md.agent.entity.dto.AgConfigDto;
import com.bsi.md.agent.repository.AgApiProxyRepository;
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

    @Autowired
    private AgApiProxyRepository agApiProxyRepository;

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
        agApiProxyRepository.deleteByConfigId( cf.getId() );
        for( int i=0;i<arr.size();i++ ){
            JSONObject obj = arr.getJSONObject(i);
            JSONObject inputNode = obj.getJSONObject("inputNodeConfig");
            if( "apiUp".equals(inputNode.getString("type")) ){
                AgApiProxy api = new AgApiProxy();
                api.setId( obj.getString("jobId") );
                api.setConfigId(config.getId());
                api.setName( obj.getString("jobName") );
                api.setPath( inputNode.getString("path") );
                api.setEnable(obj.getBoolean("enable"));
                api.setConfigValue( obj.toJSONString() );
                agApiProxyRepository.save(api);
            }else{
                AgJob job = new AgJob();
                job.setId( obj.getString("jobId") );
                job.setConfigId( config.getId() );
                job.setName( obj.getString("jobName") );
                job.setEnable(obj.getBoolean("enable"));
                job.setCron(inputNode.getString("cron"));
                job.setConfigValue( obj.toJSONString() );
                agJobRepository.save(job);
            }

        }

        //2、刷新缓存
        agJobService.refreshJob();
    }

    public void deleteConfig(AgConfigDto configDto){
        agJobRepository.deleteByConfigId( configDto.getId() );
        agConfigRepository.deleteById(configDto.getId());
        agJobService.refreshJob();
    }
}
