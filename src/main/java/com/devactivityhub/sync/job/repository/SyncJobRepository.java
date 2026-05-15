package com.devactivityhub.sync.job.repository;

import com.devactivityhub.sync.job.domain.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {

    Optional<SyncJob> findBySourceTypeAndJobName(String sourceType, String jobName);

    List<SyncJob> findAllBySourceType(String sourceType);
}
