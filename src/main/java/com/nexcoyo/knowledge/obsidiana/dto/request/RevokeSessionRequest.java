package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RevokeSessionRequest(
        @NotNull UUID actorUserId,
        String reason
) {
}
