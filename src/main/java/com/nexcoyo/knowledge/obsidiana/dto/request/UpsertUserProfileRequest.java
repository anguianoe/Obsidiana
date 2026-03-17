package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpsertUserProfileRequest(
        @Size(max = 255) String displayName,
        UUID avatarAssetId,
        String bio,
        @Size(max = 20) String locale,
        @Size(max = 100) String timezone,
        @Size(max = 120) String city,
        @Size(max = 120) String region,
        @Size(max = 120) String country
) {
}
