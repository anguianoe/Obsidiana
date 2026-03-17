package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    AuditEvent save(AuditEvent auditEvent);
    Page<AuditEvent> getWorkspaceEvents(UUID workspaceId, Pageable pageable);
    List< AuditEvent > getEntityTimeline( String entityType, UUID entityId);
}
