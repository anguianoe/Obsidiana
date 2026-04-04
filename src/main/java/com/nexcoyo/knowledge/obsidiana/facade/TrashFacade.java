package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.RestoreTrashRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.TrashRecordCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserRestoreTrashRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserTrashRecordCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.RestoreAuditResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.TrashRecordResponse;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.TrashService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrashFacade {

    private final TrashService trashService;
    private final EntityReferenceResolver refs;

    public PageResponse< TrashRecordResponse > search( TrashEntityType entityType, UUID workspaceId, TrashStatus status, UUID deletedBy, Pageable pageable) {
        TrashRecordSearchCriteria criteria = new TrashRecordSearchCriteria();
        criteria.setEntityType(entityType);
        criteria.setWorkspaceId(workspaceId);
        criteria.setStatus(status);
        criteria.setDeletedBy(deletedBy);
        return PageResponse.from(trashService.search(criteria, pageable), ApiMapper::toResponse);
    }

    public TrashRecordResponse getById(UUID id) {
        return ApiMapper.toResponse(trashService.getRequired(id));
    }

    public TrashRecordResponse moveToTrash( TrashRecordCreateRequest request) {
        TrashRecord entity = toEntity(request);
        return ApiMapper.toResponse(trashService.moveToTrash(entity));
    }

    public RestoreAuditResponse restore( UUID trashRecordId, RestoreTrashRequest request) {
        return ApiMapper.toResponse(trashService.restore(trashRecordId, request.restoredBy(), request.reason()));
    }

    public List<TrashRecordResponse> overdue() {
        return trashService.findOverdueTrash().stream().map(ApiMapper::toResponse).toList();
    }

    public PageResponse< TrashRecordResponse > search( TrashEntityType entityType, UUID workspaceId, TrashStatus status, UUID deletedBy, Pageable pageable, UUID userId) {
        TrashRecordSearchCriteria criteria = new TrashRecordSearchCriteria();
        criteria.setEntityType(entityType);
        criteria.setWorkspaceId(workspaceId);
        criteria.setStatus(status);
        criteria.setDeletedBy(deletedBy);
        return PageResponse.from(trashService.search(criteria, pageable, userId), ApiMapper::toResponse);
    }

    public TrashRecordResponse getById(UUID id, UUID userId) {
        return ApiMapper.toResponse(trashService.getRequired(id, userId));
    }

    public TrashRecordResponse moveToTrashForUser( UserTrashRecordCreateRequest request, UUID userId) {
        TrashRecord entity = toEntity(request);
        return ApiMapper.toResponse(trashService.moveToTrash(entity, userId));
    }

    public RestoreAuditResponse restoreForUser( UUID trashRecordId, UserRestoreTrashRequest request, UUID userId) {
        return ApiMapper.toResponse(trashService.restore(trashRecordId, userId, request.reason(), userId));
    }

    public List<TrashRecordResponse> overdue(UUID userId) {
        return trashService.findOverdueTrash(userId).stream().map(ApiMapper::toResponse).toList();
    }

    private TrashRecord toEntity(TrashRecordCreateRequest request) {
        TrashRecord entity = new TrashRecord();
        if (request.id() != null) {
            entity.setId(request.id());
        }
        entity.setEntityType(request.entityType());
        entity.setEntityId(request.entityId());
        entity.setWorkspace(refs.workspace(request.workspaceId()));
        entity.setPage(refs.page(request.pageId()));
        entity.setAsset(refs.asset(request.assetId()));
        entity.setComment(refs.comment(request.commentId()));
        entity.setDeletedBy(refs.user(request.deletedBy()));
        entity.setDeleteReason(request.deleteReason());
        entity.setSnapshotPayload(request.snapshotPayload());
        entity.setDeletedAt( Instant.now());
        entity.setRestoreDeadlineAt(request.restoreDeadlineAt());
        entity.setPurgeScheduledAt(request.purgeScheduledAt());
        entity.setStatus(request.status());
        return entity;
    }

    private TrashRecord toEntity(UserTrashRecordCreateRequest request) {
        TrashRecord entity = new TrashRecord();
        if (request.id() != null) {
            entity.setId(request.id());
        }
        entity.setEntityType(request.entityType());
        entity.setEntityId(request.entityId());
        entity.setWorkspace(refs.workspace(request.workspaceId()));
        entity.setPage(refs.page(request.pageId()));
        entity.setAsset(refs.asset(request.assetId()));
        entity.setComment(refs.comment(request.commentId()));
        entity.setDeleteReason(request.deleteReason());
        entity.setSnapshotPayload(request.snapshotPayload());
        entity.setDeletedAt( Instant.now());
        entity.setRestoreDeadlineAt(request.restoreDeadlineAt());
        entity.setPurgeScheduledAt(request.purgeScheduledAt());
        entity.setStatus(request.status());
        return entity;
    }

}
