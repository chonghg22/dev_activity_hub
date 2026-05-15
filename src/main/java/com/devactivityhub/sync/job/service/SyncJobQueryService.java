package com.devactivityhub.sync.job.service;

import com.devactivityhub.sync.job.dto.SyncJobResponse;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SyncJobQueryService {

    private final SyncJobRepository syncJobRepository;

    public SyncJobQueryService(SyncJobRepository syncJobRepository) {
        this.syncJobRepository = syncJobRepository;
    }

    public List<SyncJobResponse> getSyncJobs() {
        return syncJobRepository.findAll().stream()
                .map(SyncJobResponse::from)
                .toList();
    }
}
