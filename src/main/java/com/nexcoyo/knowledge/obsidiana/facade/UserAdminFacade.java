package com.nexcoyo.knowledge.obsidiana.facade;


import java.util.List;
import java.util.UUID;
import com.nexcoyo.knowledge.obsidiana.dto.request.AssignUserTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserSearchRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserPreferenceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserProfileResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserTagAssignmentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserWorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSlimResponse;
import com.nexcoyo.knowledge.obsidiana.entity.UserPreference;
import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import com.nexcoyo.knowledge.obsidiana.entity.UserTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.CreateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserDetailResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.repository.UserPreferenceRepository;
import com.nexcoyo.knowledge.obsidiana.repository.UserProfileRepository;
import com.nexcoyo.knowledge.obsidiana.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminFacade {

    private final UserAdminService userAdminService;
    private final UserProfileRepository userProfileRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional
    public UserDetailResponse create(CreateUserRequest request) {
        AppUser saved = userAdminService.create(request);
        return getById(saved.getId());
    }

    public PageResponse<UserSummaryResponse> search(UserSearchRequest request) {
        Page<AppUser> page = userAdminService.search(request);
        return PageResponse.from(page, this::toUser);
    }

    public UserDetailResponse getById(UUID userId) {
        AppUser user = userAdminService.getById(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        UserPreference preference = userPreferenceRepository.findByUserId(userId).orElse(null);
        List<UserWorkspaceMembershipResponse> memberships = userAdminService.listUserWorkspaceMemberships(userId, null)
                .stream()
                .map(this::toMembership)
                .toList();
        List<UserTagAssignmentResponse> tags = userAdminService.listUserTags(userId, null, null)
                .stream()
                .map(this::toTagAssignment)
                .toList();
        return new UserDetailResponse(toUser(user), toProfile(profile), toPreference(preference), memberships, tags);
    }

    @Transactional
    public UserDetailResponse update(UUID userId, UpdateUserRequest request) {
        userAdminService.update(userId, request);
        return getById(userId);
    }

    @Transactional
    public UserDetailResponse updateStatus(UUID userId, UpdateUserStatusRequest request) {
        userAdminService.updateStatus(userId, request);
        return getById(userId);
    }

    public List<UserWorkspaceMembershipResponse> listUserWorkspaceMemberships(UUID userId, String status) {
        return userAdminService.listUserWorkspaceMemberships(userId, status)
                .stream()
                .map(this::toMembership)
                .toList();
    }

    @Transactional
    public UserWorkspaceMembershipResponse upsertWorkspaceMembership(UUID userId, UpsertUserWorkspaceMembershipRequest request) {
        return toMembership(userAdminService.upsertWorkspaceMembership(userId, request));
    }

    @Transactional
    public UserWorkspaceMembershipResponse updateWorkspaceMembership(UUID userId, UUID workspaceId, UpdateUserWorkspaceMembershipRequest request) {
        return toMembership(userAdminService.updateWorkspaceMembership(userId, workspaceId, request));
    }

    @Transactional
    public void removeWorkspaceMembership(UUID userId, UUID workspaceId, UUID actorUserId) {
        userAdminService.removeWorkspaceMembership(userId, workspaceId, actorUserId);
    }

    public List<UserTagAssignmentResponse> listUserTags(UUID userId, UUID workspaceId, String assignmentStatus) {
        return userAdminService.listUserTags(userId, workspaceId, assignmentStatus)
                .stream()
                .map(this::toTagAssignment)
                .toList();
    }

    @Transactional
    public UserTagAssignmentResponse assignTag(UUID userId, AssignUserTagRequest request) {
        return toTagAssignment(userAdminService.assignTag(userId, request));
    }

    @Transactional
    public void removeTag(UUID userId, UUID workspaceId, UUID tagId) {
        userAdminService.removeTag(userId, workspaceId, tagId);
    }

    private UserSummaryResponse toUser(AppUser user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getSystemRole() == null ? null : user.getSystemRole().name(),
                user.getStatus() == null ? null : user.getStatus().name(),
                user.getFirstLoginAt(),
                user.getHasCompletedOnboarding(),
                user.getOnboardingVersion(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private UserProfileResponse toProfile(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        return new UserProfileResponse(
                profile.getUser().getId(),
                profile.getDisplayName(),
                profile.getAvatarAsset() == null ? null : profile.getAvatarAsset().getId(),
                profile.getBio(),
                profile.getLocale(),
                profile.getTimezone(),
                profile.getCity(),
                profile.getRegion(),
                profile.getCountry(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private UserPreferenceResponse toPreference(UserPreference preference) {
        if (preference == null) {
            return null;
        }
        return new UserPreferenceResponse(
                preference.getUser().getId(),
                preference.getTheme(),
                preference.getSidebarCollapsed(),
                preference.getShowPrivateFirst(),
                preference.getCreatedAt(),
                preference.getUpdatedAt()
        );
    }

    private UserWorkspaceMembershipResponse toMembership(WorkspaceMembership membership) {
        Workspace workspace = membership.getWorkspace();
        return new UserWorkspaceMembershipResponse(
                membership.getId(),
                membership.getUser().getId(),
                new WorkspaceSlimResponse(
                        workspace.getId(),
                        workspace.getName(),
                        workspace.getSlug(),
                        workspace.getKind() == null ? null : workspace.getKind().name(),
                        workspace.getStatus() == null ? null : workspace.getStatus().name(),
                        workspace.getApprovalStatus() == null ? null : workspace.getApprovalStatus().name(),
                        workspace.getDescription()
                ),
                membership.getRole() == null ? null : membership.getRole().name(),
                membership.getStatus() == null ? null : membership.getStatus().name(),
                membership.getJoinedAt(),
                membership.getInvitedAt(),
                membership.getCreatedBy() == null ? null : membership.getCreatedBy().getId(),
                membership.getCreatedAt(),
                membership.getUpdatedAt()
        );
    }

    private UserTagAssignmentResponse toTagAssignment(UserTagAssignment assignment) {
        return new UserTagAssignmentResponse(
                assignment.getId(),
                assignment.getTargetUser().getId(),
                assignment.getWorkspace().getId(),
                assignment.getTag().getId(),
                assignment.getTag().getName(),
                assignment.getAssignmentStatus() == null ? null : assignment.getAssignmentStatus().name(),
                assignment.getCreatedBy() == null ? null : assignment.getCreatedBy().getId(),
                assignment.getCreatedAt()
        );
    }
}
