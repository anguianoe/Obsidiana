package com.nexcoyo.knowledge.obsidiana.dto.request;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateUserWorkspaceMembershipRequest(
        String role,
        String status,
        UUID actorUserId,
        Instant invitedAt,
        Instant joinedAt
) {
}
