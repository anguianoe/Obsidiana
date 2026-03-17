package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignUserTagRequest(
        @NotNull UUID workspaceId,
        @NotNull UUID tagId,
        UUID actorUserId,
        String assignmentStatus
) {
}
