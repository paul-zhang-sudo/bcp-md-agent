package com.bsi.md.agent.repository;

import com.bsi.md.agent.entity.AgJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * @author fish
 */
@Component
public interface AgJobRepository extends JpaRepository<AgJob, Long> {
}
