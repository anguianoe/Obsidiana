package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.ReactionType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageCommentReactionResponse( UUID id, UUID commentId, UUID userId, ReactionType reactionType, OffsetDateTime createdAt) {}
