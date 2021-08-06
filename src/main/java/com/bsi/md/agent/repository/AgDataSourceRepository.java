package com.bsi.md.agent.repository;

import com.bsi.md.agent.entity.AgDataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * @author fish
 */
@Component
public interface AgDataSourceRepository extends JpaRepository<AgDataSource, Long> {
}
