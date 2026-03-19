package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateApprovalStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.repository.UserProfileRepository;
import com.nexcoyo.knowledge.obsidiana.service.WorkspaceService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceFacade {

    private final WorkspaceService workspaceService;
    private final EntityReferenceResolver refs;
    private final UserProfileRepository userProfileRepository;

    public PageResponse< WorkspaceResponse > search( String text, WorkspaceKind kind, WorkspaceStatus status, UUID createdBy, Pageable pageable) {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setNameOrSlug(text);
        criteria.setKind(kind);
        criteria.setStatus(status);

        Page<Workspace> page = workspaceService.search(criteria, pageable);
        return toEnrichedPage(page, workspaceId -> workspaceService.getRequired(workspaceId));
    }

    public WorkspaceResponse getById(UUID id, UUID createdBy) {
        return toEnrichedResponse(workspaceService.getRequired(id, createdBy));
    }

    public WorkspaceResponse adminGetById(UUID id) {
        return toEnrichedResponse(workspaceService.getRequired(id));
    }

    public WorkspaceResponse save( WorkspaceUpsertRequest request, UUID createdBy, Boolean isAdmin) {

        Instant now = Instant.now();
        Workspace entity = request.id() == null ? new Workspace() : workspaceService.getRequired(request.id(), createdBy);
        if (Boolean.TRUE.equals(isAdmin)) {
            entity = request.id() == null ? new Workspace() : workspaceService.getRequired(request.id());
        }

        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setKind(request.kind());

        if(entity.getCreatedBy() == null){
            entity.setCreatedBy(refs.user(createdBy));
            entity.setCreatedAt(now);
        }
        if(request.kind() == WorkspaceKind.PRIVATE){
            entity.setApprovedBy( refs.user(createdBy) );
            entity.setApprovedAt( now );
            entity.setApprovalStatus( com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus.APPROVED );
        }
        entity.setUpdatedAt( now );
        entity.setDescription(request.description());

        Workspace saved = workspaceService.save(entity);

        // Reload to ensure lazy associations used in response mapping are initialized.
        Workspace reloaded = Boolean.TRUE.equals(isAdmin)
            ? workspaceService.getRequired(saved.getId())
            : workspaceService.getRequired(saved.getId(), createdBy);

        return toEnrichedResponse(reloaded);
    }

    public List< WorkspaceSummaryResponse > accessible( UUID userId) {
        return workspaceService.findAccessibleSummaries(userId).stream().map(ApiMapper::toResponse).toList();
    }

    public List< WorkspaceMembershipResponse > activeMembers( UUID workspaceId) {
        return toMembershipResponses(workspaceService.getActiveMembers(workspaceId));
    }

    public List< WorkspaceMembershipResponse > activeMembers( UUID workspaceId, UUID userId) {
        return toMembershipResponses(workspaceService.getActiveMembers(workspaceId, userId));
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
        Page<Workspace> page = workspaceService.listAll(status, pageable);
        return toEnrichedPage(page, workspaceId -> workspaceService.getRequired(workspaceId));
    }

    public PageResponse< WorkspaceResponse > searchByCreatedBy(UUID createdBy, String text, WorkspaceStatus status, Pageable pageable) {
        Page<Workspace> page = workspaceService.searchByCreatedBy(createdBy, text, status, pageable);
        return toEnrichedPage(page, workspaceId -> workspaceService.getRequired(workspaceId, createdBy));
    }

    public List< WorkspaceResponse > pendingGroupApprovals() {
        return workspaceService.listPendingGroupApprovals().stream()
            .map(workspace -> workspaceService.getRequired(workspace.getId()))
            .map(this::toEnrichedResponse)
            .toList();
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
        return toMembershipResponse(workspaceService.updateMemberRole(workspaceId, memberId, role, actorUserId, isAdmin));
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

    private WorkspaceResponse toEnrichedResponse(com.nexcoyo.knowledge.obsidiana.entity.Workspace workspace) {
        UserProfile creatorProfile = workspace.getCreatedBy() == null ? null
                : userProfileRepository.findByUserId(workspace.getCreatedBy().getId()).orElse(null);
        return ApiMapper.toResponse(workspace, creatorProfile);
    }

    private List<WorkspaceMembershipResponse> toMembershipResponses(List<WorkspaceMembership> memberships) {
        if (memberships == null || memberships.isEmpty()) {
            return List.of();
        }

        Map<UUID, UserProfile> profilesByUserId = userProfileRepository.findAllByUserIdIn(
                memberships.stream()
                        .map(WorkspaceMembership::getUser)
                        .filter(java.util.Objects::nonNull)
                        .map(com.nexcoyo.knowledge.obsidiana.entity.AppUser::getId)
                        .toList()
        ).stream().collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        return memberships.stream()
                .map(membership -> {
                    UUID userId = membership.getUser() == null ? null : membership.getUser().getId();
                    return toMembershipResponse(membership, userId == null ? null : profilesByUserId.get(userId));
                })
                .toList();
    }

    private WorkspaceMembershipResponse toMembershipResponse(WorkspaceMembership membership) {
        UserProfile profile = membership.getUser() == null
                ? null
                : userProfileRepository.findDetailedByUserId(membership.getUser().getId()).orElse(null);
        return toMembershipResponse(membership, profile);
    }

    private WorkspaceMembershipResponse toMembershipResponse(WorkspaceMembership membership, UserProfile profile) {
        return new WorkspaceMembershipResponse(
                membership.getId(),
                membership.getWorkspace() == null ? null : membership.getWorkspace().getId(),
                membership.getUser() == null ? null : membership.getUser().getId(),
                membership.getUser() == null ? null : membership.getUser().getUsername(),
                membership.getUser() == null ? null : membership.getUser().getStatus(),
                profile == null ? null : profile.getDisplayName(),
                profile == null || profile.getAvatarAsset() == null ? null : profile.getAvatarAsset().getId(),
                membership.getRole(),
                membership.getStatus(),
                membership.getJoinedAt(),
                membership.getInvitedAt(),
                membership.getCreatedBy() == null ? null : membership.getCreatedBy().getId(),
                membership.getCreatedAt(),
                membership.getUpdatedAt()
        );
    }

    private PageResponse<WorkspaceResponse> toEnrichedPage(Page<Workspace> page, java.util.function.Function<UUID, Workspace> loader) {
        List<WorkspaceResponse> content = page.getContent().stream()
            .map(workspace -> loader.apply(workspace.getId()))
            .map(this::toEnrichedResponse)
            .toList();

        return new PageResponse<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }
}
