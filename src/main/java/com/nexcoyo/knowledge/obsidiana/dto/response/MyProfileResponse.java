package com.nexcoyo.knowledge.obsidiana.dto.response;

public record MyProfileResponse(
        UserSummaryResponse user,
        UserProfileResponse profile,
        UserPreferenceResponse preference
) {
}
