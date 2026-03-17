package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String displayName,
        UUID avatarAssetId,
        String bio,
        String locale,
        String timezone,
        String city,
        String region,
        String country,
        Instant createdAt,
        Instant updatedAt
) {
}
