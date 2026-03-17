package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceMembershipResponse(
        UUID id, UUID workspaceId, UUID userId, WorkspaceRole role, MembershipStatus status,
        Instant joinedAt, Instant invitedAt, UUID createdBy, Instant createdAt, Instant updatedAt
) {}
