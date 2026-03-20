package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;
import com.nexcoyo.knowledge.obsidiana.dto.request.AssignUserTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.CreateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserSearchRequest;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.UserTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import org.springframework.data.domain.Page;


public interface UserAdminService {

    Page<AppUser> search(UserSearchRequest request);

    AppUser getById(UUID userId);

    AppUser create(CreateUserRequest request);

    AppUser update(UUID userId, UpdateUserRequest request);

    AppUser updateStatus(UUID userId,UpdateUserStatusRequest request);

    List<WorkspaceMembership> listUserWorkspaceMemberships(UUID userId, String status);

    WorkspaceMembership upsertWorkspaceMembership(UUID userId, UpsertUserWorkspaceMembershipRequest request, UUID actorUserId);

    WorkspaceMembership updateWorkspaceMembership(UUID userId, UUID workspaceId, UpdateUserWorkspaceMembershipRequest request, UUID actorUserId);

    void removeWorkspaceMembership(UUID userId, UUID workspaceId, UUID actorUserId);

    List<UserTagAssignment> listUserTags(UUID userId, UUID workspaceId, String assignmentStatus);

    UserTagAssignment assignTag(UUID userId, AssignUserTagRequest request);

    void removeTag(UUID userId, UUID workspaceId, UUID tagId);
}
