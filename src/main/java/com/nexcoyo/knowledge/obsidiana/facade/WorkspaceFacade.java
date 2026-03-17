package com.nexcoyo.knowledge.obsidiana.facade;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.WorkspaceService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceFacade {

    private final WorkspaceService workspaceService;
    private final EntityReferenceResolver refs;

    public PageResponse< WorkspaceResponse > search( String text, WorkspaceKind kind, WorkspaceStatus status, UUID createdBy, Pageable pageable) {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setNameOrSlug(text);
        criteria.setKind(kind);
        criteria.setStatus(status);
        return PageResponse.from(workspaceService.search(criteria, pageable), ApiMapper::toResponse);
    }

    public WorkspaceResponse getById(UUID id) {
        return ApiMapper.toResponse(workspaceService.getRequired(id));
    }

    public WorkspaceResponse save( WorkspaceUpsertRequest request) {
        Workspace entity = request.id() == null ? new Workspace() : workspaceService.getRequired(request.id());
        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setKind(request.kind());
        entity.setStatus(request.status());
        entity.setApprovalStatus(request.approvalStatus());
        entity.setCreatedBy(refs.user(request.createdBy()));
        entity.setApprovedBy(refs.user(request.approvedBy()));
        entity.setDescription(request.description());
        return ApiMapper.toResponse(workspaceService.save(entity));
    }

    public List< WorkspaceSummaryResponse > accessible( UUID userId) {
        return workspaceService.findAccessibleSummaries(userId).stream().map(ApiMapper::toResponse).toList();
    }

    public List< WorkspaceMembershipResponse > activeMembers( UUID workspaceId) {
        return workspaceService.getActiveMembers(workspaceId).stream().map(ApiMapper::toResponse).toList();
    }

    public List< WorkspaceInvitationResponse > pendingInvitations( UUID workspaceId) {
        return workspaceService.getPendingInvitations(workspaceId).stream().map(ApiMapper::toResponse).toList();
    }
}
