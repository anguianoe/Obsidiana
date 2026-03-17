package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.Valid;

public record UpdateMyProfileRequest(
        @Valid UpsertUserProfileRequest profile,
        @Valid UpsertUserPreferenceRequest preference
) {
}
