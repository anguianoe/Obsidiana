package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 120) String username,
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        String oldPassword,

        @Size(min = 8, max = 50, message = "New Password must be between 8 and 50 characters")
        String newPassword,
        String systemRole,
        Boolean hasCompletedOnboarding,
        @Size(max = 20) String onboardingVersion,
        @Valid UpsertUserProfileRequest profile,
        @Size(max = 120, message = "Roles must not exceed 120 characters") String roles,
        @Valid UpsertUserPreferenceRequest preference
) {
}
