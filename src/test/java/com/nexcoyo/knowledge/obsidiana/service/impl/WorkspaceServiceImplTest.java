package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceInvitationRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.util.enums.InvitationStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private WorkspaceMembershipRepository workspaceMembershipRepository;
    @Mock
    private WorkspaceInvitationRepository workspaceInvitationRepository;
    @Mock
    private EntityManager entityManager;

    private WorkspaceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkspaceServiceImpl(workspaceRepository, workspaceMembershipRepository, workspaceInvitationRepository);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
    }

    @Test
    void inviteMemberPopulatesRequiredFieldsAndCorrectAssignments() {
        UUID workspaceId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        AppUser owner = user(UUID.randomUUID(), "owner@example.com");
        AppUser invitedUser = user(invitedUserId, "invitee@example.com");
        AppUser actor = user(actorUserId, "admin@example.com");
        Workspace workspace = workspace(workspaceId, owner);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(entityManager.getReference(AppUser.class, invitedUserId)).thenReturn(invitedUser);
        when(entityManager.getReference(AppUser.class, actorUserId)).thenReturn(actor);
        when(workspaceInvitationRepository.findByWorkspaceIdAndInvitedUser(workspaceId, invitedUser)).thenReturn(Optional.empty());
        when(workspaceInvitationRepository.save(any(WorkspaceInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkspaceInvitation saved = service.inviteMember(workspaceId, invitedUserId, WorkspaceRole.EDITOR, actorUserId, true);

        assertThat(saved.getWorkspace()).isSameAs(workspace);
        assertThat(saved.getInvitedUser()).isSameAs(invitedUser);
        assertThat(saved.getInvitedBy()).isSameAs(actor);
        assertThat(saved.getInvitedEmail()).isEqualTo("invitee@example.com");
        assertThat(saved.getRole()).isEqualTo(WorkspaceRole.EDITOR);
        assertThat(saved.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(saved.getInvitationTokenHash()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().plusSeconds(6 * 24 * 60 * 60));
        assertThat(saved.getAcceptedAt()).isNull();
        assertThat(saved.getRejectedAt()).isNull();
        assertThat(saved.getRevokedAt()).isNull();
    }

    @Test
    void inviteMemberChecksOwnershipUsingActorUserIdForNonAdminFlow() {
        UUID workspaceId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        AppUser actor = user(actorUserId, "owner@example.com");
        AppUser invitedUser = user(invitedUserId, "invitee@example.com");
        Workspace workspace = workspace(workspaceId, actor);

        when(workspaceRepository.findByIdAndCreatedById(workspaceId, actorUserId)).thenReturn(Optional.of(workspace));
        when(entityManager.getReference(AppUser.class, invitedUserId)).thenReturn(invitedUser);
        when(entityManager.getReference(AppUser.class, actorUserId)).thenReturn(actor);
        when(workspaceInvitationRepository.findByWorkspaceIdAndInvitedUser(workspaceId, invitedUser)).thenReturn(Optional.empty());
        when(workspaceInvitationRepository.save(any(WorkspaceInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        service.inviteMember(workspaceId, invitedUserId, WorkspaceRole.VIEWER, actorUserId, false);

        verify(workspaceRepository).findByIdAndCreatedById(workspaceId, actorUserId);
    }

    @Test
    void respondToInvitationAcceptsForInvitedUser() {
        UUID invitationId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        AppUser invitedUser = user(invitedUserId, "invitee@example.com");
        WorkspaceInvitation invitation = pendingInvitation(invitationId, invitedUser, Instant.now().plusSeconds(3600));

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceInvitationRepository.save(any(WorkspaceInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkspaceInvitation saved = service.respondToInvitation(invitationId, "ACCEPT", invitedUserId);

        assertThat(saved.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(saved.getAcceptedAt()).isNotNull();
        assertThat(saved.getRejectedAt()).isNull();
    }

    @Test
    void respondToInvitationRejectsForInvitedUser() {
        UUID invitationId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        AppUser invitedUser = user(invitedUserId, "invitee@example.com");
        WorkspaceInvitation invitation = pendingInvitation(invitationId, invitedUser, Instant.now().plusSeconds(3600));

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceInvitationRepository.save(any(WorkspaceInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkspaceInvitation saved = service.respondToInvitation(invitationId, "REJECT", invitedUserId);

        assertThat(saved.getStatus()).isEqualTo(InvitationStatus.REJECTED);
        assertThat(saved.getRejectedAt()).isNotNull();
        assertThat(saved.getAcceptedAt()).isNull();
    }

    @Test
    void respondToInvitationRejectsDifferentUser() {
        UUID invitationId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        WorkspaceInvitation invitation = pendingInvitation(invitationId, user(invitedUserId, "invitee@example.com"), Instant.now().plusSeconds(3600));

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.respondToInvitation(invitationId, "ACCEPT", otherUserId))
            .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
            .hasMessageContaining("Invitation not found");
    }

    @Test
    void respondToInvitationRejectsNonPendingInvitation() {
        UUID invitationId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        WorkspaceInvitation invitation = pendingInvitation(invitationId, user(invitedUserId, "invitee@example.com"), Instant.now().plusSeconds(3600));
        invitation.setStatus(InvitationStatus.ACCEPTED);

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.respondToInvitation(invitationId, "REJECT", invitedUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no longer pending");
    }

    @Test
    void respondToInvitationRejectsExpiredInvitation() {
        UUID invitationId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        WorkspaceInvitation invitation = pendingInvitation(invitationId, user(invitedUserId, "invitee@example.com"), Instant.now().minusSeconds(60));

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.respondToInvitation(invitationId, "ACCEPT", invitedUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expired");
    }

    @Test
    void respondToInvitationRejectsUnsupportedResponseValue() {
        UUID invitationId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();
        WorkspaceInvitation invitation = pendingInvitation(invitationId, user(invitedUserId, "invitee@example.com"), Instant.now().plusSeconds(3600));

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.respondToInvitation(invitationId, "MAYBE", invitedUserId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported invitation response");
    }

    @Test
    void saveCreatesOwnerMembershipForNewWorkspace() {
        UUID ownerUserId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        AppUser owner = user(ownerUserId, "owner@example.com");
        Workspace workspace = new Workspace();
        workspace.setCreatedBy(owner);
        workspace.setKind(WorkspaceKind.PRIVATE);
        workspace.setApprovalStatus(ApprovalStatus.REJECTED);

        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace saved = invocation.getArgument(0);
            saved.setId(workspaceId);
            return saved;
        });
        when(workspaceMembershipRepository.save(any(WorkspaceMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Workspace savedWorkspace = service.save(workspace);

        assertThat(savedWorkspace.getId()).isEqualTo(workspaceId);
        assertThat(savedWorkspace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(savedWorkspace.getStatus()).isEqualTo(WorkspaceStatus.ACTIVE);

        ArgumentCaptor<WorkspaceMembership> membershipCaptor = ArgumentCaptor.forClass(WorkspaceMembership.class);
        verify(workspaceMembershipRepository).save(membershipCaptor.capture());

        WorkspaceMembership savedMembership = membershipCaptor.getValue();
        assertThat(savedMembership.getWorkspace()).isSameAs(savedWorkspace);
        assertThat(savedMembership.getUser()).isSameAs(owner);
        assertThat(savedMembership.getRole()).isEqualTo(WorkspaceRole.OWNER);
        assertThat(savedMembership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(savedMembership.getCreatedBy()).isSameAs(owner);
        assertThat(savedMembership.getJoinedAt()).isNotNull();
        assertThat(savedMembership.getInvitedAt()).isNull();
    }

    @Test
    void saveDoesNotCreateOwnerMembershipForExistingWorkspace() {
        UUID workspaceId = UUID.randomUUID();
        AppUser owner = user(UUID.randomUUID(), "owner@example.com");
        Workspace workspace = workspace(workspaceId, owner);
        workspace.setKind(WorkspaceKind.GROUP);
        workspace.setApprovalStatus(ApprovalStatus.APPROVED);

        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Workspace savedWorkspace = service.save(workspace);

        assertThat(savedWorkspace).isSameAs(workspace);
        assertThat(savedWorkspace.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(savedWorkspace.getStatus()).isEqualTo(WorkspaceStatus.PENDING);
        verify(workspaceMembershipRepository, never()).save(any(WorkspaceMembership.class));
    }

    @Test
    void saveRejectsWorkspaceWithoutKind() {
        Workspace workspace = new Workspace();
        workspace.setCreatedBy(user(UUID.randomUUID(), "owner@example.com"));

        assertThatThrownBy(() -> service.save(workspace))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Workspace kind is required");

        verify(workspaceRepository, never()).save(any(Workspace.class));
        verify(workspaceMembershipRepository, never()).save(any(WorkspaceMembership.class));
    }

    @Test
    void deleteAllowsCreator() {
        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        Workspace workspace = workspace(workspaceId, user(creatorId, "creator@example.com"));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        service.delete(workspaceId, creatorId);

        assertThat(workspace.getStatus()).isEqualTo(WorkspaceStatus.ARCHIVED);
        assertThat(workspace.getDeletedAt()).isNotNull();
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void deleteAllowsActiveAdminMembership() {
        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID adminMemberId = UUID.randomUUID();

        Workspace workspace = workspace(workspaceId, user(creatorId, "creator@example.com"));
        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspace(workspace);
        membership.setUser(user(adminMemberId, "admin@example.com"));
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setRole(WorkspaceRole.ADMIN);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, adminMemberId)).thenReturn(Optional.of(membership));

        service.delete(workspaceId, adminMemberId);

        assertThat(workspace.getStatus()).isEqualTo(WorkspaceStatus.ARCHIVED);
        assertThat(workspace.getDeletedAt()).isNotNull();
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void deleteRejectsNonPrivilegedMembership() {
        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID editorMemberId = UUID.randomUUID();

        Workspace workspace = workspace(workspaceId, user(creatorId, "creator@example.com"));
        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspace(workspace);
        membership.setUser(user(editorMemberId, "editor@example.com"));
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setRole(WorkspaceRole.EDITOR);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, editorMemberId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> service.delete(workspaceId, editorMemberId))
            .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
            .hasMessageContaining("access denied");

        verify(workspaceRepository, never()).save(any(Workspace.class));
    }

    private static AppUser user(UUID id, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private static Workspace workspace(UUID id, AppUser createdBy) {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        workspace.setCreatedBy(createdBy);
        return workspace;
    }

    private static WorkspaceInvitation pendingInvitation(UUID id, AppUser invitedUser, Instant expiresAt) {
        WorkspaceInvitation invitation = new WorkspaceInvitation();
        invitation.setId(id);
        invitation.setInvitedUser(invitedUser);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(expiresAt);
        return invitation;
    }
}
