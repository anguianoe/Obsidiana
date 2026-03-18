package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;

public record RespondInvitationRequest(
    @NotNull String response
) {}

