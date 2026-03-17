package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentThreadResponse(UUID commentId, UUID parentCommentId, UUID authorUserId, String body, OffsetDateTime createdAt, Long reactionCount, Long replyCount) {}
