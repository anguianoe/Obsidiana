package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.CommentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageCommentResponse(
        UUID id, UUID pageId, UUID workspaceId, UUID authorUserId, UUID parentCommentId,
        String body, CommentStatus commentStatus, OffsetDateTime createdAt, OffsetDateTime editedAt, OffsetDateTime deletedAt
) {}
