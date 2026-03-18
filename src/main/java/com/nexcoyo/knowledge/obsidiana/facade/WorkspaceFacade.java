package com.nexcoyo.knowledge.obsidiana.facade;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateApprovalStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateWorkspaceStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.WorkspaceService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
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

    public WorkspaceResponse getById(UUID id, UUID createdBy) {
        return ApiMapper.toResponse(workspaceService.getRequired(id, createdBy));
    }

    public WorkspaceResponse adminGetById(UUID id) {
        return ApiMapper.toResponse(workspaceService.getRequired(id));
    }

    public WorkspaceResponse save( WorkspaceUpsertRequest request, UUID createdBy, Boolean isAdmin) {

        Workspace entity = request.id() == null ? new Workspace() : workspaceService.getRequired(request.id(), createdBy);
        if (Boolean.TRUE.equals(isAdmin)) {
            entity = request.id() == null ? new Workspace() : workspaceService.getRequired(request.id());
        }

        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setKind(request.kind());
        entity.setStatus(request.status());
        entity.setApprovalStatus(request.approvalStatus());

        if(entity.getCreatedBy() == null){
            entity.setCreatedBy(refs.user(createdBy));
        }

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

    public List< WorkspaceMembershipResponse > activeMembers( UUID workspaceId, UUID userId) {
        return workspaceService.getActiveMembers(workspaceId, userId).stream().map(ApiMapper::toResponse).toList();
    }

    public List< WorkspaceInvitationResponse > pendingInvitations( UUID workspaceId) {
        return workspaceService.getPendingInvitations(workspaceId).stream().map(ApiMapper::toResponse).toList();
    }

    public void delete(UUID workspaceId) {
        workspaceService.delete(workspaceId);
    }

    public void delete(UUID workspaceId, UUID userId) {
        workspaceService.delete(workspaceId, userId);
    }

    public PageResponse< WorkspaceResponse > listAll(WorkspaceStatus status, Pageable pageable) {
        return PageResponse.from(workspaceService.listAll(status, pageable), ApiMapper::toResponse);
    }

    public PageResponse< WorkspaceResponse > searchByCreatedBy(UUID createdBy, String text, WorkspaceStatus status, Pageable pageable) {
        return PageResponse.from(workspaceService.searchByCreatedBy(createdBy, text, status, pageable), ApiMapper::toResponse);
    }

    public WorkspaceResponse setInactive(UUID workspaceId) {
        return ApiMapper.toResponse(workspaceService.setInactive(workspaceId));
    }

    public WorkspaceResponse setInactive(UUID workspaceId, UUID userId) {
        return ApiMapper.toResponse(workspaceService.setInactive(workspaceId, userId));
    }

    public WorkspaceResponse updateApprovalStatus(UUID workspaceId, UpdateApprovalStatusRequest request) {
        return ApiMapper.toResponse(workspaceService.updateApprovalStatus(workspaceId, request.approvalStatus(), request.approvedBy()));
    }

    // ========== MEMBER MANAGEMENT ==========

    public WorkspaceInvitationResponse inviteMember( UUID workspaceId, UUID userId, WorkspaceRole role, UUID actorUserId, Boolean isAdmin) {
        return ApiMapper.toResponse(workspaceService.inviteMember(workspaceId, userId, role, actorUserId, isAdmin));
    }

    public WorkspaceMembershipResponse updateMemberRole(UUID workspaceId, UUID memberId, String role, UUID actorUserId, boolean isAdmin) {
        return ApiMapper.toResponse(workspaceService.updateMemberRole(workspaceId, memberId, role, actorUserId, isAdmin));
    }

    public void removeMember(UUID workspaceId, UUID memberId, UUID actorUserId, boolean isAdmin) {
        workspaceService.removeMember(workspaceId, memberId, actorUserId, isAdmin);
    }

    // ========== INVITATION HANDLING ==========

    public List< WorkspaceInvitationResponse > myInvitations(UUID userId) {
        return workspaceService.myInvitations( userId).stream().map(ApiMapper::toResponse).toList();
    }

    public WorkspaceInvitationResponse respondToInvitation(UUID invitationId, String response, UUID userId) {
        return ApiMapper.toResponse(workspaceService.respondToInvitation(invitationId, response, userId));
    }

    // ========== RESTORATION ==========

    public WorkspaceResponse restoreWorkspace(UUID workspaceId) {
        return ApiMapper.toResponse(workspaceService.restoreWorkspace(workspaceId));
    }

    public WorkspaceResponse restoreWorkspace(UUID workspaceId, UUID userId) {
        return ApiMapper.toResponse(workspaceService.restoreWorkspace(workspaceId, userId));
    }
}
