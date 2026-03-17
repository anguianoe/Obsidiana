package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RevokeAllMyOtherSessionsRequest(
        @NotNull UUID actorUserId,
        UUID currentSessionId,
        String reason
) {
}
