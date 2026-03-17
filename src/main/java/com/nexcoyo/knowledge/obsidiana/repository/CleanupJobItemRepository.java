package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.CleanupJobItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CleanupJobItemRepository extends JpaRepository< CleanupJobItem, UUID> {
    List<CleanupJobItem> findAllByJobId(UUID jobId);
}
