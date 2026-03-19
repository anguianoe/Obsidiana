package com.nexcoyo.knowledge.obsidiana.facade;

import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.repository.UserProfileRepository;
import com.nexcoyo.knowledge.obsidiana.service.WorkspaceService;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceFacadeTest {

    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private EntityReferenceResolver refs;
    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private WorkspaceFacade workspaceFacade;

    @Test
    void activeMembersIncludesUserAndProfileFieldsWhenAvailable() {
        UUID workspaceId = UUID.randomUUID();
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();
        UUID avatarAssetId = UUID.randomUUID();

        WorkspaceMembership firstMembership = membership(workspaceId, firstUserId, "darketzero", UserStatus.ACTIVE);
        WorkspaceMembership secondMembership = membership(workspaceId, secondUserId, "miguel", UserStatus.BLOCKED);

        UserProfile firstProfile = new UserProfile();
        firstProfile.setUserId(firstUserId);
        firstProfile.setDisplayName("Miguel Angel Anguiano");
        StoredAsset avatarAsset = new StoredAsset();
        avatarAsset.setId(avatarAssetId);
        firstProfile.setAvatarAsset(avatarAsset);

        when(workspaceService.getActiveMembers(workspaceId)).thenReturn(List.of(firstMembership, secondMembership));
        when(userProfileRepository.findAllByUserIdIn(anyCollection())).thenReturn(List.of(firstProfile));

        List<WorkspaceMembershipResponse> response = workspaceFacade.activeMembers(workspaceId);

        assertThat(response).hasSize(2);

        WorkspaceMembershipResponse first = response.getFirst();
        assertThat(first.userId()).isEqualTo(firstUserId);
        assertThat(first.username()).isEqualTo("darketzero");
        assertThat(first.userStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(first.displayName()).isEqualTo("Miguel Angel Anguiano");
        assertThat(first.avatarAssetId()).isEqualTo(avatarAssetId);

        WorkspaceMembershipResponse second = response.get(1);
        assertThat(second.userId()).isEqualTo(secondUserId);
        assertThat(second.username()).isEqualTo("miguel");
        assertThat(second.userStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(second.displayName()).isNull();
        assertThat(second.avatarAssetId()).isNull();
    }

    @Test
    void updateMemberRoleIncludesProfileFieldsWhenAvailable() {
        UUID workspaceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        WorkspaceMembership membership = membership(workspaceId, memberId, "editor-user", UserStatus.ACTIVE);

        UserProfile profile = new UserProfile();
        profile.setUserId(memberId);
        profile.setDisplayName("Editor User");

        when(workspaceService.updateMemberRole(workspaceId, memberId, "EDITOR", actorUserId, false)).thenReturn(membership);
        when(userProfileRepository.findDetailedByUserId(memberId)).thenReturn(Optional.of(profile));

        WorkspaceMembershipResponse response = workspaceFacade.updateMemberRole(workspaceId, memberId, "EDITOR", actorUserId, false);

        assertThat(response.username()).isEqualTo("editor-user");
        assertThat(response.userStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.displayName()).isEqualTo("Editor User");
        assertThat(response.avatarAssetId()).isNull();
        verify(userProfileRepository).findDetailedByUserId(memberId);
    }

    @Test
    void activeMembersReturnsEmptyListWhenServiceReturnsEmpty() {
        UUID workspaceId = UUID.randomUUID();

        when(workspaceService.getActiveMembers(workspaceId)).thenReturn(List.of());

        assertThat(workspaceFacade.activeMembers(workspaceId)).isEmpty();
    }

    private static WorkspaceMembership membership(UUID workspaceId, UUID userId, String username, UserStatus userStatus) {
        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);

        AppUser user = new AppUser();
        user.setId(userId);
        user.setUsername(username);
        user.setStatus(userStatus);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setId(UUID.randomUUID());
        membership.setWorkspace(workspace);
        membership.setUser(user);
        membership.setRole(WorkspaceRole.EDITOR);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setJoinedAt(Instant.now());
        membership.setCreatedAt(Instant.now());
        membership.setUpdatedAt(Instant.now());
        return membership;
    }
}

