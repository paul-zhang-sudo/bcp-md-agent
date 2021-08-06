package com.bsi.md.agent.repository;

import com.bsi.md.agent.entity.AgConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * @author fish
 */
@Component
public interface AgConfigRepository extends JpaRepository<AgConfig, Long> {
}
