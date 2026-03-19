package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(
    UUID id,
    String name,
    String slug,
    WorkspaceKind kind,
    WorkspaceStatus status,
    ApprovalStatus approvalStatus,
    UUID createdBy,
    CreatorSummary creator,
    UUID approvedBy,
    Instant approvedAt,
    String description,
    Instant createdAt,
    Instant updatedAt
) {
    public record CreatorSummary(
        UUID userId,
        String username,
        UserStatus userStatus,
        String displayName,
        UUID avatarAssetId,
        String country
    ) {}
}
