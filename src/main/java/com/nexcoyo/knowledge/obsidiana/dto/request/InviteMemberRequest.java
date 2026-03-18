package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InviteMemberRequest(
    @NotNull UUID userId,
    @NotNull WorkspaceRole role
) {}

