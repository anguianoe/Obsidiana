package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignUserTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.CreateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserPreferenceRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserProfileRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserSearchRequest;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.UserPreference;
import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import com.nexcoyo.knowledge.obsidiana.entity.UserTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.UserPreferenceRepository;
import com.nexcoyo.knowledge.obsidiana.repository.UserProfileRepository;
import com.nexcoyo.knowledge.obsidiana.repository.UserTagAssignmentRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceTagRepository;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMembershipRepository workspaceMembershipRepository;
    @Mock private WorkspaceTagRepository workspaceTagRepository;
    @Mock private UserTagAssignmentRepository userTagAssignmentRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private UserPreferenceRepository userPreferenceRepository;
    @Mock private EntityManager entityManager;
    @Mock private PasswordEncoder passwordEncoder;

    private UserAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserAdminServiceImpl(
                appUserRepository,
                workspaceRepository,
                workspaceMembershipRepository,
                workspaceTagRepository,
                userTagAssignmentRepository,
                userProfileRepository,
                userPreferenceRepository,
                entityManager,
                passwordEncoder
        );
    }

    @Test
    void searchUsesRequestedPagingAndSortingBounds() {
        UserSearchRequest request = new UserSearchRequest(
                null, null, null, null, null, null, null,
                null, null, null,
                -1, 999, "username", "asc"
        );

        when(appUserRepository.findAll(org.mockito.ArgumentMatchers.<Specification<AppUser>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.search(request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(appUserRepository).findAll(org.mockito.ArgumentMatchers.<Specification<AppUser>>any(), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(200);
        assertThat(pageable.getSort().getOrderFor("username").isAscending()).isTrue();
    }

    @Test
    void getByIdReturnsUser() {
        UUID userId = UUID.randomUUID();
        AppUser user = user(userId, "mail@test.com", "u");
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));

        AppUser result = service.getById(userId);

        assertThat(result).isSameAs(user);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createPersistsUserProfileAndPreference() {
        UUID userId = UUID.randomUUID();
        UUID avatarId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest(
                "  USER@MAIL.COM  ",
                "  user-name  ",
                "plain-secret",
                "",
                null,
                true,
                "1.0",
                new UpsertUserProfileRequest("Display", avatarId, "bio", "es_MX", "America/Mexico_City", "GDL", "JAL", "MX"),
                new UpsertUserPreferenceRequest("dark", false, null),
                "USER"
        );

        StoredAsset avatar = new StoredAsset();
        when(appUserRepository.existsByEmail("user@mail.com")).thenReturn(false);
        when(appUserRepository.existsByUsername("user-name")).thenReturn(false);
        when(passwordEncoder.encode("plain-secret")).thenReturn("encoded-secret");
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(userId);
            return u;
        });
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(entityManager.getReference(StoredAsset.class, avatarId)).thenReturn(avatar);

        AppUser saved = service.create(request);

        assertThat(saved.getId()).isEqualTo(userId);
        assertThat(saved.getEmail()).isEqualTo("user@mail.com");
        assertThat(saved.getUsername()).isEqualTo("user-name");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-secret");
        assertThat(saved.getSystemRole()).isEqualTo(SystemRole.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).saveAndFlush(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getAvatarAsset()).isSameAs(avatar);

        ArgumentCaptor<UserPreference> preferenceCaptor = ArgumentCaptor.forClass(UserPreference.class);
        verify(userPreferenceRepository).saveAndFlush(preferenceCaptor.capture());
        assertThat(preferenceCaptor.getValue().getShowPrivateFirst()).isTrue();
    }

    @Test
    void createThrowsWhenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "exists@test.com", "username", "p", "USER", "ACTIVE", true, "1.0", null, null, "USER"
        );
        when(appUserRepository.existsByEmail("exists@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateChangesPasswordOnlyWhenOldPasswordMatches() {
        UUID userId = UUID.randomUUID();
        AppUser existing = user(userId, "old@mail.com", "old-user");
        existing.setPasswordHash("existing-hash");
        existing.setSystemRole(SystemRole.USER);
        existing.setStatus(UserStatus.ACTIVE);

        UpdateUserRequest request = new UpdateUserRequest(
                "new@mail.com",
                "new-user",
                "correct-old",
                "new-password",
                "SUPER_ADMIN",
                false,
                "2.0",
                null,
                "SUPER_ADMIN",
                null
        );

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(appUserRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(appUserRepository.existsByUsername("new-user")).thenReturn(false);
        when(passwordEncoder.matches("correct-old", "existing-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser updated = service.update(userId, request);

        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
        assertThat(updated.getUsername()).isEqualTo("new-user");
        assertThat(updated.getPasswordHash()).isEqualTo("encoded-new");
        assertThat(updated.getSystemRole()).isEqualTo(SystemRole.SUPER_ADMIN);
        assertThat(updated.getHasCompletedOnboarding()).isFalse();
        verify(userProfileRepository, never()).saveAndFlush(any(UserProfile.class));
        verify(userPreferenceRepository, never()).saveAndFlush(any(UserPreference.class));
    }

    @Test
    void updateThrowsWhenOldPasswordIsInvalid() {
        UUID userId = UUID.randomUUID();
        AppUser existing = user(userId, "old@mail.com", "old-user");
        existing.setPasswordHash("existing-hash");

        UpdateUserRequest request = new UpdateUserRequest(
                "old@mail.com", "old-user", "wrong-old", "new-password", null, null, null, null, null, null
        );

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(appUserRepository.existsByEmail("old@mail.com")).thenReturn(true);
        when(appUserRepository.existsByUsername("old-user")).thenReturn(true);
        when(passwordEncoder.matches("wrong-old", "existing-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.update(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid current password");
    }

    @Test
    void updateStatusMarksDeletedAndClearsDeletedAtWhenReactivated() {
        UUID userId = UUID.randomUUID();
        AppUser user = user(userId, "u@test.com", "u");
        user.setStatus(UserStatus.ACTIVE);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser deleted = service.updateStatus(userId, new UpdateUserStatusRequest("DELETED"));
        assertThat(deleted.getDeletedAt()).isNotNull();

        AppUser reactivated = service.updateStatus(userId, new UpdateUserStatusRequest("ACTIVE"));
        assertThat(reactivated.getDeletedAt()).isNull();
    }

    @Test
    void listUserWorkspaceMembershipsUsesStatusFilterWhenProvided() {
        UUID userId = UUID.randomUUID();
        AppUser user = user(userId, "u@test.com", "u");
        WorkspaceMembership membership = new WorkspaceMembership();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workspaceMembershipRepository.findAllByUserIdAndStatus(userId, MembershipStatus.ACTIVE))
                .thenReturn(List.of(membership));

        List<WorkspaceMembership> result = service.listUserWorkspaceMemberships(userId, "active");

        assertThat(result).containsExactly(membership);
    }

    @Test
    void listUserWorkspaceMembershipsUsesSpecificationWhenStatusMissing() {
        UUID userId = UUID.randomUUID();
        AppUser user = user(userId, "u@test.com", "u");
        WorkspaceMembership membership = new WorkspaceMembership();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workspaceMembershipRepository.findAll(org.mockito.ArgumentMatchers.<Specification<WorkspaceMembership>>any())).thenReturn(List.of(membership));

        List<WorkspaceMembership> result = service.listUserWorkspaceMemberships(userId, null);

        assertThat(result).containsExactly(membership);
    }

    @Test
    void upsertWorkspaceMembershipCreatesNewMembership() {
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        AppUser user = user(userId, "user@test.com", "user");
        AppUser actor = user(actorId, "actor@test.com", "actor");
        Workspace workspace = workspace(workspaceId);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(Optional.empty());
        when(workspaceMembershipRepository.save(any(WorkspaceMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceMembership saved = service.upsertWorkspaceMembership(
                userId,
                new UpsertUserWorkspaceMembershipRequest(workspaceId, "editor"),
                actorId
        );

        assertThat(saved.getWorkspace()).isSameAs(workspace);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getCreatedBy()).isSameAs(actor);
        assertThat(saved.getRole()).isEqualTo(WorkspaceRole.EDITOR);
        assertThat(saved.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(saved.getJoinedAt()).isNotNull();
        assertThat(saved.getInvitedAt()).isNotNull();
    }

    @Test
    void updateWorkspaceMembershipUpdatesRoleAndStatus() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AppUser user = user(userId, "user@test.com", "user");
        AppUser actorRef = user(actorId, "actor@test.com", "actor");
        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setRole(WorkspaceRole.VIEWER);
        membership.setStatus(MembershipStatus.INVITED);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(Optional.of(membership));
        when(entityManager.getReference(AppUser.class, actorId)).thenReturn(actorRef);
        when(workspaceMembershipRepository.save(any(WorkspaceMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceMembership updated = service.updateWorkspaceMembership(
                userId,
                workspaceId,
                new UpdateUserWorkspaceMembershipRequest("ADMIN", "ACTIVE"),
                actorId
        );

        assertThat(updated.getRole()).isEqualTo(WorkspaceRole.ADMIN);
        assertThat(updated.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(updated.getCreatedBy()).isSameAs(actorRef);
    }

    @Test
    void removeWorkspaceMembershipMarksAsRemoved() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        AppUser user = user(userId, "user@test.com", "user");
        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setStatus(MembershipStatus.ACTIVE);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(Optional.of(membership));
        when(workspaceMembershipRepository.save(any(WorkspaceMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.removeWorkspaceMembership(userId, workspaceId, null);

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.REMOVED);
    }

    @Test
    void listUserTagsRoutesToExpectedRepositoryMethod() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        AppUser user = user(userId, "user@test.com", "user");

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTagAssignmentRepository.findAllByTargetUserIdAndWorkspaceIdAndAssignmentStatus(
                userId, workspaceId, AssignmentStatus.ACTIVE))
                .thenReturn(List.of(new UserTagAssignment()));

        List<UserTagAssignment> result = service.listUserTags(userId, workspaceId, "active");

        assertThat(result).hasSize(1);
    }

    @Test
    void assignTagCreatesAssignmentWhenMissing() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        AppUser targetUser = user(userId, "user@test.com", "user");
        AppUser actorRef = user(actorUserId, "actor@test.com", "actor");
        Workspace workspace = workspace(workspaceId);
        WorkspaceTag tag = new WorkspaceTag();
        tag.setId(tagId);
        tag.setWorkspace(workspace);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceTagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(userTagAssignmentRepository.findByTargetUserIdAndWorkspaceIdAndTagId(userId, workspaceId, tagId))
                .thenReturn(Optional.empty());
        when(entityManager.getReference(AppUser.class, actorUserId)).thenReturn(actorRef);
        when(userTagAssignmentRepository.save(any(UserTagAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserTagAssignment saved = service.assignTag(
                userId,
                new AssignUserTagRequest(workspaceId, tagId, actorUserId, "ACTIVE")
        );

        assertThat(saved.getTargetUser()).isSameAs(targetUser);
        assertThat(saved.getWorkspace()).isSameAs(workspace);
        assertThat(saved.getTag()).isSameAs(tag);
        assertThat(saved.getAssignmentStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        assertThat(saved.getCreatedBy()).isSameAs(actorRef);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void assignTagThrowsWhenTagBelongsToAnotherWorkspace() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID otherWorkspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        AppUser targetUser = user(userId, "user@test.com", "user");
        Workspace requestedWorkspace = workspace(workspaceId);
        Workspace tagWorkspace = workspace(otherWorkspaceId);
        WorkspaceTag tag = new WorkspaceTag();
        tag.setId(tagId);
        tag.setWorkspace(tagWorkspace);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(requestedWorkspace));
        when(workspaceTagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        assertThatThrownBy(() -> service.assignTag(userId, new AssignUserTagRequest(workspaceId, tagId, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to the workspace");
    }

    @Test
    void removeTagSetsAssignmentAsInactive() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        UserTagAssignment assignment = new UserTagAssignment();
        assignment.setAssignmentStatus(AssignmentStatus.ACTIVE);

        when(userTagAssignmentRepository.findByTargetUserIdAndWorkspaceIdAndTagId(userId, workspaceId, tagId))
                .thenReturn(Optional.of(assignment));

        service.removeTag(userId, workspaceId, tagId);

        assertThat(assignment.getAssignmentStatus()).isEqualTo(AssignmentStatus.INACTIVE);
        verify(userTagAssignmentRepository).save(assignment);
    }

    @Test
    void removeTagThrowsWhenAssignmentMissing() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        when(userTagAssignmentRepository.findByTargetUserIdAndWorkspaceIdAndTagId(userId, workspaceId, tagId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeTag(userId, workspaceId, tagId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User tag assignment not found");
    }

    @Test
    void createSkipsProfileAndPreferenceWhenRequestsAreNull() {
        UUID userId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest(
                "mail@test.com", "username", "plain", "USER", "ACTIVE", true, "1.0", null, null, "USER"
        );

        when(appUserRepository.existsByEmail("mail@test.com")).thenReturn(false);
        when(appUserRepository.existsByUsername("username")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(userId);
            return u;
        });

        service.create(request);

        verify(userProfileRepository, never()).saveAndFlush(any(UserProfile.class));
        verify(userPreferenceRepository, never()).saveAndFlush(any(UserPreference.class));
    }

    @Test
    void updateAllowsKeepingSameEmailAndUsernameWhenAlreadyExisting() {
        UUID userId = UUID.randomUUID();
        AppUser existing = user(userId, "same@mail.com", "same-user");
        existing.setPasswordHash("hash");

        UpdateUserRequest request = new UpdateUserRequest(
                "same@mail.com", "same-user", null, null, null, null, null, null, null, null
        );

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(appUserRepository.existsByEmail("same@mail.com")).thenReturn(true);
        when(appUserRepository.existsByUsername("same-user")).thenReturn(true);
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = service.update(userId, request);

        assertThat(result.getEmail()).isEqualTo("same@mail.com");
        assertThat(result.getUsername()).isEqualTo("same-user");
        verify(appUserRepository, atLeastOnce()).findById(userId);
    }

    @Test
    void upsertWorkspaceMembershipThrowsWhenWorkspaceMissing() {
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        AppUser user = user(userId, "user@test.com", "user");
        AppUser actor = user(actorId, "actor@test.com", "actor");

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertWorkspaceMembership(
                userId,
                new UpsertUserWorkspaceMembershipRequest(workspaceId, "VIEWER"),
                actorId
        )).isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Workspace not found");
    }

    @Test
    void updateWorkspaceMembershipThrowsWhenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        AppUser user = user(userId, "user@test.com", "user");

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateWorkspaceMembership(
                userId,
                workspaceId,
                new UpdateUserWorkspaceMembershipRequest("EDITOR", "ACTIVE"),
                null
        )).isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Workspace membership not found");
    }

    @Test
    void removeWorkspaceMembershipThrowsWhenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        AppUser user = user(userId, "user@test.com", "user");

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeWorkspaceMembership(userId, workspaceId, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workspace membership not found");
    }

    @Test
    void listUserTagsFallbacksToByUserWhenNoFilters() {
        UUID userId = UUID.randomUUID();
        AppUser user = user(userId, "user@test.com", "user");

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTagAssignmentRepository.findAllByTargetUserId(userId)).thenReturn(List.of(new UserTagAssignment()));

        List<UserTagAssignment> result = service.listUserTags(userId, null, null);

        assertThat(result).hasSize(1);
    }

    private static AppUser user(UUID id, String email, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        user.setSystemRole(SystemRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private static Workspace workspace(UUID id) {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        return workspace;
    }
}


