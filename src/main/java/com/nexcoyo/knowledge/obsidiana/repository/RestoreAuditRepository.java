package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.RestoreAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestoreAuditRepository extends JpaRepository<RestoreAudit, UUID> {
    List< RestoreAudit > findAllByTrashRecordIdOrderByRestoredAtDesc( UUID trashRecordId);
}
