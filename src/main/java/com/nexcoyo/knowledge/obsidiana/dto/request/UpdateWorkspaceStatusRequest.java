package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkspaceStatusRequest(
    @NotNull WorkspaceStatus status
) {}

