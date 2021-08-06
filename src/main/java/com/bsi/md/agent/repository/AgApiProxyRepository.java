package com.bsi.md.agent.repository;

import com.bsi.md.agent.entity.AgApiProxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface AgApiProxyRepository extends JpaRepository<AgApiProxy, Long> {
}
