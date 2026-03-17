package com.nexcoyo.knowledge.obsidiana.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.CleanupJob;
import com.nexcoyo.knowledge.obsidiana.util.enums.CleanupJobStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.CleanupJobType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CleanupJobRepository extends JpaRepository< CleanupJob, UUID> {
    Optional<CleanupJob> findTopByJobTypeOrderByStartedAtDesc( CleanupJobType jobType);
    List<CleanupJob> findAllByStatusOrderByStartedAtDesc( CleanupJobStatus status);
}
