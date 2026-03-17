package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserStatusRequest(
        @NotBlank String status
) {
}
