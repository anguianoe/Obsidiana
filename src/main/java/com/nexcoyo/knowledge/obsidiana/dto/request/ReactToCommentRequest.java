package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReactToCommentRequest(@NotNull UUID commentId, @NotNull UUID userId, @NotNull ReactionType reactionType) {}
