package com.nexcoyo.knowledge.obsidiana.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSessionSearchRequest(
        UUID userId,
        String sessionStatus,
        String text,
        OffsetDateTime loginFrom,
        OffsetDateTime loginTo,
        OffsetDateTime expiresBefore,
        Boolean activeOnly,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) {
}
