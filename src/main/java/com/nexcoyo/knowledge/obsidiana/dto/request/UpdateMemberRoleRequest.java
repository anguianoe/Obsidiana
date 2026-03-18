package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
    @NotNull String role
) {}

