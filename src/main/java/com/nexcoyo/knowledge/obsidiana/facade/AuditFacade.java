package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.AuditEventCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.AuditEventResponse;
import com.nexcoyo.knowledge.obsidiana.entity.AuditEvent;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditFacade {

    private final AuditService auditService;
    private final EntityReferenceResolver refs;

    public AuditEventResponse save( AuditEventCreateRequest request) {
        AuditEvent event = new AuditEvent();
        if (request.id() != null) {
            event.setId(request.id());
        }
        event.setEventType(request.eventType());
        event.setEntityType(request.entityType());
        event.setEntityId(request.entityId());
        event.setActorUser(refs.user(request.actorUserId()));
        event.setWorkspace(refs.workspace(request.workspaceId()));
        event.setEventPayload(request.eventPayload());
        event.setCreatedAt(OffsetDateTime.now());
        return ApiMapper.toResponse(auditService.save(event));
    }

    public PageResponse<AuditEventResponse> workspaceEvents( UUID workspaceId, Pageable pageable) {
        return PageResponse.from(auditService.getWorkspaceEvents(workspaceId, pageable), ApiMapper::toResponse);
    }

    public List<AuditEventResponse> timeline(String entityType, UUID entityId) {
        return auditService.getEntityTimeline(entityType, entityId).stream().map( ApiMapper::toResponse).toList();
    }
}
