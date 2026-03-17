package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    Page< AuditEvent > findAllByWorkspaceId( UUID workspaceId, Pageable pageable);
    List<AuditEvent> findTop100ByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);
}
