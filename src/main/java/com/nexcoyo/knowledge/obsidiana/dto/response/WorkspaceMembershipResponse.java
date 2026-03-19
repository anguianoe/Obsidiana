package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceMembershipResponse(
        UUID id,
        UUID workspaceId,
        UUID userId,
        String username,
        UserStatus userStatus,
        String displayName,
        UUID avatarAssetId,
        WorkspaceRole role,
        MembershipStatus status,
        Instant joinedAt,
        Instant invitedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
