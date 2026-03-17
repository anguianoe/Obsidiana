package com.nexcoyo.knowledge.obsidiana.records;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String refreshToken,
        @NotBlank String deviceId
) {}

