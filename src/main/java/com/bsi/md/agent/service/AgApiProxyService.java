package com.bsi.md.agent.service;

import com.bsi.framework.core.service.FwService;
import com.bsi.md.agent.entity.AgApiProxy;
import com.bsi.md.agent.repository.AgApiProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 计划任务service
 */

@Service
@Transactional
public class AgApiProxyService extends FwService {
    @Autowired
    private AgApiProxyRepository agApiProxyRepository;

    /**
     * 查询所有启用的实时集成配置
     * @return List<AgApiProxy>
     */
    public List<AgApiProxy> findAllEnable(){
        AgApiProxy example = new AgApiProxy();
        example.setEnable(true);
        return agApiProxyRepository.findAll(Example.of(example));
    }
}