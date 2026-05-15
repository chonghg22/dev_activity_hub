package com.devactivityhub.activity.external.repository;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ExternalActivityRepository extends JpaRepository<ExternalActivity, Long>, JpaSpecificationExecutor<ExternalActivity> {

    Optional<ExternalActivity> findBySourceTypeAndSourceId(String sourceType, String sourceId);
}
