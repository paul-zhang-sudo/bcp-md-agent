package com.bsi.md.agent.service;

import com.bsi.framework.core.service.FwService;
import com.bsi.md.agent.entity.AgJob;
import com.bsi.md.agent.repository.AgJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * 计划任务service
 */

@Service
@Transactional
public class AgConfigService extends FwService {
}
