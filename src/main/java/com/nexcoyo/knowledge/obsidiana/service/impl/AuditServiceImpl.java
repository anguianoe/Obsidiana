package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AuditEvent;
import com.nexcoyo.knowledge.obsidiana.repository.AuditEventRepository;
import com.nexcoyo.knowledge.obsidiana.service.AuditService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    @Override
    @Transactional
    public AuditEvent save( AuditEvent auditEvent) {
        return auditEventRepository.save(auditEvent);
    }

    @Override
    public Page<AuditEvent> getWorkspaceEvents(UUID workspaceId, Pageable pageable) {
        return auditEventRepository.findAllByWorkspaceId(workspaceId, pageable);
    }

    @Override
    public List<AuditEvent> getEntityTimeline(String entityType, UUID entityId) {
        return auditEventRepository.findTop100ByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }
}
