package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTagRequest(@NotNull UUID pageId, @NotNull UUID workspaceId, @NotNull UUID tagId, UUID actorUserId) {}
