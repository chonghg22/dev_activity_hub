package com.devactivityhub.sync.execution.repository;

import com.devactivityhub.sync.execution.domain.SyncExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncExecutionRepository extends JpaRepository<SyncExecution, Long> {
}
