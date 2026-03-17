package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 120) String username,
        @NotBlank String passwordHash,
        @NotBlank String systemRole,
        String status,
        Boolean hasCompletedOnboarding,
        @Size(max = 20) String onboardingVersion,
        @Valid UpsertUserProfileRequest profile,
        @Valid UpsertUserPreferenceRequest preference,
        @Size(max = 120, message = "Roles must not exceed 120 characters")
        String roles
) {
}
