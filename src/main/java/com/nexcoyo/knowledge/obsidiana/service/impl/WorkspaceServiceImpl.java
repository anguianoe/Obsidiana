package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.config.TokenHashingConfig;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.projection.WorkspaceSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceInvitationRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.service.WorkspaceService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.service.specification.WorkspaceSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.InvitationStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
public class WorkspaceServiceImpl implements WorkspaceService {

    private static final long WORKSPACE_INVITATION_TTL_DAYS = 7;

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page< Workspace > search( WorkspaceSearchCriteria criteria, Pageable pageable) {
        return workspaceRepository.findAll(WorkspaceSpecifications.byCriteria(criteria), pageable);
    }

    @Override
    public List< WorkspaceSummaryProjection > findAccessibleSummaries( UUID userId) {
        return workspaceRepository.findAccessibleWorkspaceSummaries(userId);
    }

    @Override
    public Workspace getRequired(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
    }

    @Override
    public Workspace getRequired(UUID workspaceId, UUID userId) {
        return workspaceRepository.findByIdAndCreatedById(workspaceId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
    }

    @Override
    @Transactional
    public Workspace save(Workspace workspace) {
        return workspaceRepository.save(workspace);
    }

    @Override
    public List< WorkspaceMembership > getActiveMembers( UUID workspaceId) {
        return workspaceMembershipRepository.findAllByWorkspaceIdAndStatus(workspaceId, MembershipStatus.ACTIVE);
    }

    @Override
    public List< WorkspaceMembership > getActiveMembers( UUID workspaceId, UUID userId) {
        Workspace workspace = getRequired(workspaceId, userId);
        if(workspace == null)
        {
            throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
        }
        return workspaceMembershipRepository.findAllByWorkspaceIdAndStatus(workspaceId, MembershipStatus.ACTIVE);
    }





    @Override
    public List< WorkspaceInvitation > getPendingInvitations( UUID workspaceId) {
        return workspaceInvitationRepository.findAllByWorkspaceIdAndStatus(workspaceId, InvitationStatus.PENDING);
    }

    @Override
    public Page< Workspace > listAll(WorkspaceStatus status, Pageable pageable) {
        return workspaceRepository.findAll(WorkspaceSpecifications.adminList(status), pageable);
    }

    @Override
    public Page< Workspace > searchByCreatedBy(UUID createdBy, String text, WorkspaceStatus status, Pageable pageable) {
        return workspaceRepository.findAll(WorkspaceSpecifications.byCreatedBy(createdBy, text, status), pageable);
    }

    @Override
    @Transactional
    public Workspace setInactive(UUID workspaceId) {
        Workspace workspace = getRequired(workspaceId);
        workspace.setStatus(WorkspaceStatus.INACTIVE);
        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public Workspace setInactive(UUID workspaceId, UUID userId) {
        Workspace workspace = getRequired(workspaceId, userId);
        if(workspace == null || workspace.getCreatedBy() == null || !workspace.getCreatedBy().getId().equals(userId))
        {
            throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
        }
        workspace.setStatus(WorkspaceStatus.INACTIVE);
        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public Workspace updateApprovalStatus(UUID workspaceId, ApprovalStatus approvalStatus, UUID approvedBy) {
        Workspace workspace = getRequired(workspaceId);
        workspace.setApprovalStatus(approvalStatus);
        if (approvedBy != null) {
            workspace.setApprovedBy(entityManager.getReference(AppUser.class, approvedBy));
        }
        if (approvalStatus == ApprovalStatus.APPROVED && workspace.getApprovedAt() == null) {
            workspace.setApprovedAt(Instant.now());
        }
        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public void delete(UUID workspaceId) {
        Workspace workspace = getRequired(workspaceId);
        if (workspace.getDeletedAt() == null) {
            workspace.setDeletedAt(Instant.now());
        }
        workspace.setStatus(WorkspaceStatus.ARCHIVED);
        workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public void delete(UUID workspaceId, UUID userId) {
        Workspace workspace = getRequired(workspaceId, userId);
        if(workspace == null || workspace.getCreatedBy() == null || !workspace.getCreatedBy().getId().equals(userId))
        {
            throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
        }
        if (workspace.getDeletedAt() == null) {
            workspace.setDeletedAt(Instant.now());
        }
        workspace.setStatus(WorkspaceStatus.ARCHIVED);
        workspaceRepository.save(workspace);
    }

    // ========== MEMBER MANAGEMENT ==========

    @Override
    @Transactional
    public WorkspaceInvitation inviteMember( UUID workspaceId, UUID userId, WorkspaceRole role, UUID actorUserId, Boolean isAdmin) {
        Workspace workspace;
        if (Boolean.TRUE.equals(isAdmin)) {
            workspace = getRequired(workspaceId);
        } else {
            workspace = getRequired(workspaceId, actorUserId);
        }

        if (!Boolean.TRUE.equals(isAdmin)
            && (workspace.getCreatedBy() == null || !workspace.getCreatedBy().getId().equals(actorUserId))) {
            throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
        }

        AppUser invitedUser = entityManager.getReference(AppUser.class, userId);
        AppUser actor = actorUserId != null ? entityManager.getReference(AppUser.class, actorUserId) : null;

        WorkspaceInvitation invitation = workspaceInvitationRepository
            .findByWorkspaceIdAndInvitedUser(workspaceId, invitedUser)
            .orElseGet(WorkspaceInvitation::new);

        String rawToken = TokenHashingConfig.newOpaqueToken();

        invitation.setWorkspace(workspace);
        invitation.setInvitedUser(invitedUser);
        invitation.setRole(role);
        invitation.setInvitedBy(actor);
        invitation.setInvitedEmail(invitedUser.getEmail());
        invitation.setInvitationTokenHash(TokenHashingConfig.sha256(rawToken));
        invitation.setExpiresAt(Instant.now().plus(WORKSPACE_INVITATION_TTL_DAYS, ChronoUnit.DAYS));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setAcceptedAt(null);
        invitation.setRejectedAt(null);
        invitation.setRevokedAt(null);

        if (invitation.getCreatedAt() == null) {
            invitation.setCreatedAt(Instant.now());
        }
        invitation.setUpdatedAt(Instant.now());
        return workspaceInvitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public WorkspaceMembership updateMemberRole(UUID workspaceId, UUID memberId, String role, UUID actorUserId, boolean isAdmin) {
        WorkspaceMembership membership;
        if( isAdmin ){
            membership = workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                                                      .orElseThrow(() -> new EntityNotFoundException("Membership not found: workspace=" + workspaceId + ", user=" + memberId));
        }
        else {
            Workspace workspace = getRequired(workspaceId, actorUserId);
            if(workspace == null || workspace.getCreatedBy() == null || !workspace.getCreatedBy().getId().equals(actorUserId))
            {
                throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
            }
            membership = workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Membership not found: workspace=" + workspaceId + ", user=" + memberId));
        }
        membership.setRole(com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole.valueOf(role.toUpperCase()));
        membership.setUpdatedAt(Instant.now());

        return workspaceMembershipRepository.save(membership);
    }

    @Override
    @Transactional
    public void removeMember(UUID workspaceId, UUID memberId, UUID actorUserId, boolean isAdmin) {

        WorkspaceMembership membership;
        if( isAdmin ){
            membership = workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                                                      .orElseThrow(() -> new EntityNotFoundException("Membership not found: workspace=" + workspaceId + ", user=" + memberId));
        }
        else {
            Workspace workspace = getRequired(workspaceId, actorUserId);
            if(workspace == null || workspace.getCreatedBy() == null || !workspace.getCreatedBy().getId().equals(actorUserId))
            {
                throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
            }
            membership = workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                                                      .orElseThrow(() -> new EntityNotFoundException("Membership not found: workspace=" + workspaceId + ", user=" + memberId));
        }

        membership.setStatus(MembershipStatus.REMOVED);
        membership.setUpdatedAt(Instant.now());
        workspaceMembershipRepository.save(membership);
    }

    // ========== INVITATION HANDLING ==========

    @Override
    public List< WorkspaceInvitation > myInvitations(UUID userId) {
        return workspaceInvitationRepository.findAllByInvitedUserIdAndStatus(
            userId,
            InvitationStatus.PENDING
        );
    }

    @Override
    @Transactional
    public WorkspaceInvitation respondToInvitation(UUID invitationId, String response, UUID userId) {
        WorkspaceInvitation invitation = workspaceInvitationRepository.findById(invitationId)
            .orElseThrow(() -> new EntityNotFoundException("Invitation not found: " + invitationId));

        if (invitation.getInvitedUser() == null || !userId.equals(invitation.getInvitedUser().getId())) {
            throw new EntityNotFoundException("Invitation not found: " + invitationId);
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer pending: " + invitationId);
        }

        if (invitation.getExpiresAt() != null && !Instant.now().isBefore(invitation.getExpiresAt())) {
            throw new IllegalStateException("Invitation has expired: " + invitationId);
        }

        if ("ACCEPT".equalsIgnoreCase(response)) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(Instant.now());
            invitation.setRejectedAt(null);
        } else if ("REJECT".equalsIgnoreCase(response)) {
            invitation.setStatus(InvitationStatus.REJECTED);
            invitation.setRejectedAt(Instant.now());
            invitation.setAcceptedAt(null);
        } else {
            throw new IllegalArgumentException("Unsupported invitation response: " + response);
        }

        return workspaceInvitationRepository.save(invitation);
    }

    // ========== RESTORATION ==========

    @Override
    @Transactional
    public Workspace restoreWorkspace(UUID workspaceId) {
        Workspace workspace = getRequired(workspaceId);
        workspace.setDeletedAt(null);
        workspace.setStatus(WorkspaceStatus.ACTIVE);
        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public Workspace restoreWorkspace(UUID workspaceId, UUID userId) {
        Workspace workspace = getRequired(workspaceId, userId);
        if(workspace == null || workspace.getCreatedBy() == null || !workspace.getCreatedBy().getId().equals(userId))
        {
            throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
        }
        workspace.setDeletedAt(null);
        workspace.setStatus(WorkspaceStatus.ACTIVE);
        return workspaceRepository.save(workspace);
    }
}
