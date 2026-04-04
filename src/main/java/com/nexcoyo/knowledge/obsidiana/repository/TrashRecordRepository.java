package com.nexcoyo.knowledge.obsidiana.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrashRecordRepository extends JpaRepository<TrashRecord, UUID>, JpaSpecificationExecutor<TrashRecord> {
    Optional<TrashRecord> findByEntityTypeAndEntityIdAndStatus( TrashEntityType entityType, UUID entityId, TrashStatus status);
    Optional<TrashRecord> findByIdAndDeletedById(UUID id, UUID deletedById);
    List<TrashRecord> findAllByStatusAndRestoreDeadlineAtBefore(TrashStatus status, Instant restoreDeadlineAt);
    List<TrashRecord> findAllByStatusAndRestoreDeadlineAtBeforeAndDeletedById(TrashStatus status, Instant restoreDeadlineAt, UUID deletedById);
    Page< TrashRecord > findAllByWorkspaceId( UUID workspaceId, Pageable pageable);
}
