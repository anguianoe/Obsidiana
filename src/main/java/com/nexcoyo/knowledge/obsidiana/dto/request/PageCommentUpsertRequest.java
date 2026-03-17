package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.CommentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PageCommentUpsertRequest(
    UUID id,
    @NotNull UUID pageId,
    @NotNull UUID workspaceId,
    @NotNull UUID authorUserId,
    UUID parentCommentId,
    @NotBlank String body,
    @NotNull CommentStatus commentStatus
) {}
