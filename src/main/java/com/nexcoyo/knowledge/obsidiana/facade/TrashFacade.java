package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.RestoreTrashRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.TrashRecordCreateRequest;
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
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setRestoreDeadlineAt(request.restoreDeadlineAt());
        entity.setPurgeScheduledAt(request.purgeScheduledAt());
        entity.setStatus(request.status());
        return ApiMapper.toResponse(trashService.moveToTrash(entity));
    }

    public RestoreAuditResponse restore( UUID trashRecordId, RestoreTrashRequest request) {
        return ApiMapper.toResponse(trashService.restore(trashRecordId, request.restoredBy(), request.reason()));
    }

    public List<TrashRecordResponse> overdue() {
        return trashService.findOverdueTrash().stream().map(ApiMapper::toResponse).toList();
    }
}
