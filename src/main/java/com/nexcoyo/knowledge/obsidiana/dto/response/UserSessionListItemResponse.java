package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSessionListItemResponse(
        UUID id,
        String sessionStatus,
        String deviceType,
        String browserName,
        String osName,
        String cityName,
        String regionName,
        String countryName,
        OffsetDateTime loginAt,
        OffsetDateTime expiresAt,
        OffsetDateTime revokedAt
) {
}
