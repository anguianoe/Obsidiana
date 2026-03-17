package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkPageToWorkspaceRequest(@NotNull UUID pageId, @NotNull UUID workspaceId, UUID linkedBy) {}
