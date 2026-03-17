package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserPreferenceResponse(
        UUID userId,
        String theme,
        Boolean sidebarCollapsed,
        Boolean showPrivateFirst,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
