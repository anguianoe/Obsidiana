package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserTagAssignmentResponse(
        UUID assignmentId,
        UUID targetUserId,
        UUID workspaceId,
        UUID tagId,
        String tagName,
        String assignmentStatus,
        UUID createdBy,
        OffsetDateTime createdAt
) {
}
