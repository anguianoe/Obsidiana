package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSessionResponse(
        UUID id,
        UUID userId,
        String sessionStatus,
        String ipAddress,
        String userAgent,
        String deviceType,
        String osName,
        String browserName,
        String cityName,
        String regionName,
        String countryName,
        OffsetDateTime loginAt,
        OffsetDateTime expiresAt,
        OffsetDateTime revokedAt,
        OffsetDateTime createdAt,
        Boolean current,
        Boolean revocable
) {
}
