package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String email,
        String username,
        String systemRole,
        String status,
        Instant firstLoginAt,
        Boolean hasCompletedOnboarding,
        String onboardingVersion,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {
}
