package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceInvitationRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.util.enums.InvitationStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
