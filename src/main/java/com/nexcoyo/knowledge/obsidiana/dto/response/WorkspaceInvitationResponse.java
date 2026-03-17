package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.InvitationStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceInvitationResponse(
        UUID id, UUID workspaceId, String invitedEmail, UUID invitedUserId, WorkspaceRole role, InvitationStatus status,
        UUID invitedBy, Instant expiresAt, Instant acceptedAt, Instant rejectedAt, Instant revokedAt,
        Instant createdAt, Instant updatedAt
) {}
