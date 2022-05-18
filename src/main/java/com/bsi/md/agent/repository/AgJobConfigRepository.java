package com.bsi.md.agent.repository;

import com.bsi.md.agent.entity.AgJobConfig;
import com.bsi.md.agent.entity.AgWarnMethod;
import org.apache.ibatis.annotations.Delete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * @author fish
 */
@Component
public interface AgJobConfigRepository extends JpaRepository<AgJobConfig, String> {
    @Delete("delete from md_agent_job_warn where id = ?1")
    void deleteById(String id);
}
