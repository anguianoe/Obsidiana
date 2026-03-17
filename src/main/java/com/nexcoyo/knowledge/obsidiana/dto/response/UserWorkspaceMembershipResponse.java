package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserWorkspaceMembershipResponse(
        UUID membershipId,
        UUID userId,
        WorkspaceSlimResponse workspace,
        String role,
        String status,
        Instant joinedAt,
        Instant invitedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
