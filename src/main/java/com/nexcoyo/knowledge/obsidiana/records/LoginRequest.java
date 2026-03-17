package com.nexcoyo.knowledge.obsidiana.records;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank String deviceId,

        @NotBlank(message = "username (email) is required")
        @Email(message = "username must be a valid email")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
        String password,

        boolean rememberMe
) {}