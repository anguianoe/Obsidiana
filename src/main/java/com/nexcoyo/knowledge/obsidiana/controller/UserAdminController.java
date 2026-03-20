package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.AssignUserTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.CreateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserSearchRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserDetailResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserTagAssignmentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserWorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.facade.UserAdminFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserAdminController {

    private final UserAdminFacade userAdminFacade;
    private final GeneralService generalService;

    @PostMapping
    public UserDetailResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userAdminFacade.create(request);
    }

    @GetMapping
    public PageResponse<UserSummaryResponse> search(UserSearchRequest request) {
        return userAdminFacade.search(request);
    }

    @GetMapping("/details")
    public PageResponse<UserDetailResponse> searchDetailed(UserSearchRequest request) {
        return userAdminFacade.searchDetailed(request);
    }

    @GetMapping("/{userId}")
    public UserDetailResponse getById(@PathVariable UUID userId) {
        return userAdminFacade.getById(userId);
    }

    @PutMapping("/{userId}")
    public UserDetailResponse update(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        return userAdminFacade.update(userId, request);
    }

    @PatchMapping("/{userId}/status")
    public UserDetailResponse updateStatus(@PathVariable UUID userId, @Valid @RequestBody UpdateUserStatusRequest request) {
        return userAdminFacade.updateStatus(userId, request);
    }

    @GetMapping("/{userId}/workspaces")
    public List<UserWorkspaceMembershipResponse> listWorkspaces(
            @PathVariable UUID userId,
            @RequestParam(required = false) String status
    ) {
        return userAdminFacade.listUserWorkspaceMemberships(userId, status);
    }

    @PostMapping({"/{userId}/workspaces", "/{userId}/add-to-workspaces"})
    public UserWorkspaceMembershipResponse addToWorkspace(
            @PathVariable UUID userId,
            @Valid @RequestBody UpsertUserWorkspaceMembershipRequest request
    ) {
        UUID actorId = generalService.getIdUserFromSession();
        return userAdminFacade.upsertWorkspaceMembership(userId, request, actorId);
    }

    @PutMapping("/{userId}/workspaces/{workspaceId}")
    public UserWorkspaceMembershipResponse updateWorkspaceMembership(
            @PathVariable UUID userId,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateUserWorkspaceMembershipRequest request
    ) {
        UUID actorId = generalService.getIdUserFromSession();
        return userAdminFacade.updateWorkspaceMembership(userId, workspaceId, request, actorId);
    }

    @DeleteMapping("/{userId}/workspaces/{workspaceId}")
    public void removeFromWorkspace(
            @PathVariable UUID userId,
            @PathVariable UUID workspaceId
    ) {
        UUID actorId = generalService.getIdUserFromSession();
        userAdminFacade.removeWorkspaceMembership(userId, workspaceId, actorId);
    }

    @GetMapping("/{userId}/tags")
    public List<UserTagAssignmentResponse> listUserTags(
            @PathVariable UUID userId,
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) String assignmentStatus
    ) {
        return userAdminFacade.listUserTags(userId, workspaceId, assignmentStatus);
    }

    @PostMapping("/{userId}/tags")
    public UserTagAssignmentResponse assignTag(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignUserTagRequest request
    ) {
        UUID actorId = generalService.getIdUserFromSession();
        return userAdminFacade.assignTag(userId, request, actorId);
    }

    @DeleteMapping("/{userId}/tags/{tagId}")
    public void removeTag(
            @PathVariable UUID userId,
            @PathVariable UUID tagId,
            @RequestParam UUID workspaceId
    ) {
        userAdminFacade.removeTag(userId, workspaceId, tagId);
    }
}
