package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.projection.WorkspaceSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkspaceService {
    Page< Workspace > search(WorkspaceSearchCriteria criteria, Pageable pageable);
    Page< Workspace > listAll(WorkspaceStatus status, Pageable pageable);
    Page< Workspace > searchByCreatedBy(UUID createdBy, String text, WorkspaceStatus status, Pageable pageable);
    List< WorkspaceSummaryProjection > findAccessibleSummaries(UUID userId);
    Workspace getRequired(UUID workspaceId);
    Workspace getRequired(UUID workspaceId, UUID userId);
    Workspace save(Workspace workspace);
    Workspace setInactive(UUID workspaceId);
    Workspace setInactive(UUID workspaceId, UUID userId);
    Workspace updateApprovalStatus(UUID workspaceId, ApprovalStatus approvalStatus, UUID approvedBy);
    List< WorkspaceMembership > getActiveMembers(UUID workspaceId);
    List< WorkspaceMembership > getActiveMembers(UUID workspaceId, UUID userId);
    List< WorkspaceInvitation > getPendingInvitations(UUID workspaceId);
    void delete(UUID workspaceId);
    void delete(UUID workspaceId, UUID userId);

    // ========== MEMBER MANAGEMENT ==========
    WorkspaceInvitation inviteMember( UUID workspaceId, UUID userId, WorkspaceRole role, UUID actorUserId, Boolean isAdmin);
    WorkspaceMembership updateMemberRole(UUID workspaceId, UUID memberId, String role, UUID actorUserId, boolean isAdmin);
    void removeMember(UUID workspaceId, UUID memberId, UUID actorUserId, boolean isAdmin);

    // ========== INVITATION HANDLING ==========
    List< WorkspaceInvitation > myInvitations( UUID userId);
    WorkspaceInvitation respondToInvitation(UUID invitationId, String response, UUID userId);

    // ========== RESTORATION ==========
    Workspace restoreWorkspace(UUID workspaceId);
    Workspace restoreWorkspace(UUID workspaceId, UUID userId);
}
