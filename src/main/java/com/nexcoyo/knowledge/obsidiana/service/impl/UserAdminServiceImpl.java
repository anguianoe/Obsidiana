package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignUserTagRequest;import com.nexcoyo.knowledge.obsidiana.dto.request.CreateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserStatusRequest;import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserWorkspaceMembershipRequest;import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserPreferenceRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserProfileRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserWorkspaceMembershipRequest;import com.nexcoyo.knowledge.obsidiana.dto.request.UserSearchRequest;import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
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
import com.nexcoyo.knowledge.obsidiana.service.UserAdminService;
import com.nexcoyo.knowledge.obsidiana.service.specification.AppUserSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminServiceImpl implements UserAdminService {

    private final AppUserRepository appUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final WorkspaceTagRepository workspaceTagRepository;
    private final UserTagAssignmentRepository userTagAssignmentRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<AppUser> search(UserSearchRequest request) {
        Specification<AppUser> specification = AppUserSpecifications.from(request);
        return appUserRepository.findAll(specification, pageable(request));
    }

    @Override
    public AppUser getById(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    @Override
    @Transactional
    public AppUser create(CreateUserRequest request) {

        requireUniqueEmail(request.email(), null);
        requireUniqueUsername(request.username(), null);

        AppUser user = new AppUser();
        user.setEmail(normalizeEmail(request.email()));
        user.setUsername(normalizeUsername(request.username()));
        user.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
        user.setSystemRole(parseSystemRole(request.systemRole(), SystemRole.USER));
        user.setStatus(parseUserStatus(request.status(), UserStatus.ACTIVE));
        user.setRoles( request.roles() );
        user.setHasCompletedOnboarding(Boolean.TRUE.equals(request.hasCompletedOnboarding()));
        user.setOnboardingVersion(request.onboardingVersion());

        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        AppUser saved = appUserRepository.saveAndFlush(user);
        upsertProfile(saved, request.profile());
        upsertPreference(saved, request.preference());
        return saved;
    }

    @Override
    @Transactional
    public AppUser update(UUID userId, UpdateUserRequest request) {
        AppUser user = getById(userId);

        requireUniqueEmail(request.email(), userId);
        requireUniqueUsername(request.username(), userId);

        user.setEmail(normalizeEmail(request.email()));
        user.setUsername(normalizeUsername(request.username()));
        if (request.newPassword() != null && !request.newPassword().isBlank()) {

            String providedOld = request.oldPassword();
            if (providedOld == null || !passwordEncoder.matches(providedOld, user.getPasswordHash())) {
                throw new IllegalArgumentException("Invalid current password");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        if (request.systemRole() != null && !request.systemRole().isBlank()) {
            user.setSystemRole(parseSystemRole(request.systemRole(), user.getSystemRole()));
        }
        if (request.hasCompletedOnboarding() != null) {
            user.setHasCompletedOnboarding(request.hasCompletedOnboarding());
        }

        if (request.roles() != null) {
            user.setRoles(request.roles());
        }

        user.setOnboardingVersion(request.onboardingVersion());

        AppUser saved = appUserRepository.saveAndFlush(user);
        upsertProfile(saved, request.profile());
        upsertPreference(saved, request.preference());
        return saved;
    }

    @Override
    @Transactional
    public AppUser updateStatus(UUID userId, UpdateUserStatusRequest request) {
        AppUser user = getById(userId);
        user.setStatus(parseUserStatus(request.status(), user.getStatus()));
        if (user.getStatus() == UserStatus.DELETED && user.getDeletedAt() == null) {
            user.setDeletedAt(Instant.now());
        }
        if (user.getStatus() != UserStatus.DELETED) {
            user.setDeletedAt(null);
        }
        return appUserRepository.save(user);
    }

    @Override
    public List<WorkspaceMembership> listUserWorkspaceMemberships(UUID userId, String status) {
        getById(userId);
        MembershipStatus parsed = status == null || status.isBlank() ? null : MembershipStatus.valueOf(status.trim().toUpperCase());
        if (parsed != null) {
            return workspaceMembershipRepository.findAllByUserIdAndStatus(userId, parsed);
        }
        return workspaceMembershipRepository.findAll((root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get("user").get("id"), userId);
        });
    }

    @Override
    @Transactional
    public WorkspaceMembership upsertWorkspaceMembership(UUID userId, UpsertUserWorkspaceMembershipRequest request, UUID actorUserId) {
        AppUser user = getById(userId);
        AppUser actor = getById( actorUserId );
        Instant now = Instant.now();
        Workspace workspace = requireWorkspace(request.workspaceId());

        WorkspaceMembership membership = workspaceMembershipRepository
                .findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                .orElseGet(WorkspaceMembership::new);

        membership.setWorkspace(workspace);
        membership.setUser(user);
        membership.setRole(parseWorkspaceRole(request.role(), WorkspaceRole.VIEWER));
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setInvitedAt(now);
        membership.setJoinedAt(resolveJoinedAt(membership.getJoinedAt(), now, membership.getStatus()));
        membership.setCreatedBy(actor);

        if (membership.getCreatedAt() == null) {
            membership.setCreatedAt(now);
        }
        membership.setUpdatedAt(now);

        return workspaceMembershipRepository.save(membership);
    }

    @Override
    @Transactional
    public WorkspaceMembership updateWorkspaceMembership(UUID userId, UUID workspaceId, UpdateUserWorkspaceMembershipRequest request, UUID actorUserId) {
        getById(userId);
        WorkspaceMembership membership = workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace membership not found. workspaceId=" + workspaceId + ", userId=" + userId));

        Instant now = Instant.now();
        if (request.role() != null && !request.role().isBlank()) {
            membership.setRole(parseWorkspaceRole(request.role(), membership.getRole()));
        }
        if (request.status() != null && !request.status().isBlank()) {
            membership.setStatus(parseMembershipStatus(request.status(), membership.getStatus()));
        }

        membership.setCreatedBy(resolveActor(actorUserId));
        membership.setUpdatedAt(now);

        return workspaceMembershipRepository.save(membership);
    }

    @Override
    @Transactional
    public void removeWorkspaceMembership(UUID userId, UUID workspaceId, UUID actorUserId) {
        getById(userId);
        WorkspaceMembership membership = workspaceMembershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace membership not found. workspaceId=" + workspaceId + ", userId=" + userId));
        membership.setStatus(MembershipStatus.REMOVED);
        if (actorUserId != null) {
            membership.setCreatedBy(resolveActor(actorUserId));
        }
        membership.setUpdatedAt(Instant.now());
        workspaceMembershipRepository.save(membership);
    }

    @Override
    public List<UserTagAssignment> listUserTags(UUID userId, UUID workspaceId, String assignmentStatus) {
        getById(userId);
        AssignmentStatus status = assignmentStatus == null || assignmentStatus.isBlank()
                ? null
                : AssignmentStatus.valueOf(assignmentStatus.trim().toUpperCase());
        if (workspaceId != null && status != null) {
            return userTagAssignmentRepository.findAllByTargetUserIdAndWorkspaceIdAndAssignmentStatus(userId, workspaceId, status);
        }
        if (workspaceId != null) {
            return userTagAssignmentRepository.findAllByTargetUserIdAndWorkspaceId(userId, workspaceId);
        }
        if (status != null) {
            return userTagAssignmentRepository.findAllByTargetUserIdAndAssignmentStatus(userId, status);
        }
        return userTagAssignmentRepository.findAllByTargetUserId(userId);
    }

    @Override
    @Transactional
    public UserTagAssignment assignTag(UUID userId, AssignUserTagRequest request) {
        AppUser targetUser = getById(userId);
        Workspace workspace = requireWorkspace(request.workspaceId());
        WorkspaceTag tag = workspaceTagRepository.findById(request.tagId())
                .orElseThrow(() -> new EntityNotFoundException("Workspace tag not found: " + request.tagId()));
        if (!workspace.getId().equals(tag.getWorkspace().getId())) {
            throw new IllegalArgumentException("The tag does not belong to the workspace");
        }

        UserTagAssignment assignment = userTagAssignmentRepository
                .findByTargetUserIdAndWorkspaceIdAndTagId(userId, workspace.getId(), tag.getId())
                .orElseGet(UserTagAssignment::new);

        assignment.setTargetUser(targetUser);
        assignment.setWorkspace(workspace);
        assignment.setTag(tag);
        assignment.setAssignmentStatus(parseAssignmentStatus(request.assignmentStatus(), AssignmentStatus.ACTIVE));
        assignment.setCreatedBy(resolveActor(request.actorUserId()));
        if (assignment.getCreatedAt() == null) {
            assignment.setCreatedAt(OffsetDateTime.now());
        }
        return userTagAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void removeTag(UUID userId, UUID workspaceId, UUID tagId) {
        UserTagAssignment assignment = userTagAssignmentRepository
                .findByTargetUserIdAndWorkspaceIdAndTagId(userId, workspaceId, tagId)
                .orElseThrow(() -> new EntityNotFoundException("User tag assignment not found"));
        assignment.setAssignmentStatus(AssignmentStatus.INACTIVE);
        userTagAssignmentRepository.save(assignment);
    }

    /* Method Validated by Angel */
    private void upsertProfile(AppUser user, UpsertUserProfileRequest request) {

        if (request == null) {
            return;
        }

        Instant now = Instant.now();
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseGet(UserProfile::new);

        // For @MapsId, setting user is enough; Hibernate derives user_id on insert.
        profile.setUser(user);
        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        profile.setLocale(request.locale());
        profile.setTimezone(request.timezone());
        profile.setCity(request.city());
        profile.setRegion(request.region());
        profile.setCountry(request.country());
        profile.setAvatarAsset(request.avatarAssetId() == null ? null : entityManager.getReference(StoredAsset.class, request.avatarAssetId()));
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(now);
        }
        profile.setUpdatedAt(now);

        userProfileRepository.saveAndFlush(profile);
    }

    private void upsertPreference(AppUser user, UpsertUserPreferenceRequest request) {
        if (request == null) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        UserPreference preference = userPreferenceRepository.findByUserId(user.getId()).orElseGet(UserPreference::new);

        // @MapsId keeps user_id aligned with the associated AppUser.
        preference.setUser(user);
        preference.setTheme(request.theme());
        preference.setSidebarCollapsed(Boolean.TRUE.equals(request.sidebarCollapsed()));
        preference.setShowPrivateFirst(request.showPrivateFirst() == null || request.showPrivateFirst());
        if (preference.getCreatedAt() == null) {
            preference.setCreatedAt(now);
        }
        preference.setUpdatedAt(now);

        userPreferenceRepository.saveAndFlush(preference);
    }

    private Pageable pageable(UserSearchRequest request) {
        int page = request != null && request.page() != null ? Math.max(0, request.page()) : 0;
        int size = request != null && request.size() != null ? Math.min(Math.max(request.size(), 1), 200) : 20;
        String sortBy = request != null && request.sortBy() != null && !request.sortBy().isBlank() ? request.sortBy() : "createdAt";
        Sort.Direction direction = request != null && "asc".equalsIgnoreCase(request.sortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    /* Method Validated by Angel */
    private void requireUniqueEmail(String email, UUID currentUserId) {

        String normalizedEmail = normalizeEmail(email);

        if (!appUserRepository.existsByEmail(normalizedEmail)) {
            return;
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        AppUser existing = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUserId));

        if (!existing.getEmail().equals( normalizedEmail )) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

    }
    /* Method Validated by Angel */
    private void requireUniqueUsername(String username, UUID currentUserId) {
        String normalizedUsername = normalizeUsername(username);
        if (!appUserRepository.existsByUsername(normalizedUsername)) {
            return;
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        AppUser existing = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUserId));
        if (!existing.getUsername().equals(normalizedUsername)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    private Workspace requireWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
    }

    private AppUser resolveActor(UUID actorUserId) {
        return actorUserId == null ? null : entityManager.getReference(AppUser.class, actorUserId);
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizeUsername(String value) {
        return value == null ? null : value.trim();
    }

    private Instant resolveJoinedAt(Instant currentValue, Instant requestedValue, MembershipStatus status) {
        if (requestedValue != null) {
            return requestedValue;
        }
        if (currentValue != null) {
            return currentValue;
        }
        return status == MembershipStatus.ACTIVE ? Instant.now() : null;
    }

    /* Method Validated by Angel */
    private SystemRole parseSystemRole(String value, SystemRole fallback) {
        return value == null || value.isBlank() ? fallback : SystemRole.valueOf(value.trim().toUpperCase());
    }

    /* Method Validated by Angel */
    private UserStatus parseUserStatus(String value, UserStatus fallback) {
        return value == null || value.isBlank() ? fallback : UserStatus.valueOf(value.trim().toUpperCase());
    }

    private WorkspaceRole parseWorkspaceRole(String value, WorkspaceRole fallback) {
        return value == null || value.isBlank() ? fallback : WorkspaceRole.valueOf(value.trim().toUpperCase());
    }

    private MembershipStatus parseMembershipStatus(String value, MembershipStatus fallback) {
        return value == null || value.isBlank() ? fallback : MembershipStatus.valueOf(value.trim().toUpperCase());
    }

    private AssignmentStatus parseAssignmentStatus(String value, AssignmentStatus fallback) {
        return value == null || value.isBlank() ? fallback : AssignmentStatus.valueOf(value.trim().toUpperCase());
    }
}
