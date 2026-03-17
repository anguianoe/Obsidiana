package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpsertUserWorkspaceMembershipRequest(
        @NotNull UUID workspaceId,
        @NotBlank String role,
        String status,
        UUID actorUserId,
        Instant invitedAt,
        Instant joinedAt
) {
}
