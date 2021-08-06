package com.bsi.md.agent.repository;

import com.bsi.md.agent.entity.AgDataSource;
import com.bsi.md.agent.entity.AgJobParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface AgJobParamRepository extends JpaRepository<AgJobParam, Long> {

}
