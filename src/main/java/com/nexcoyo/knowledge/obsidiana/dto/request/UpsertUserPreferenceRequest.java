package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.Size;

public record UpsertUserPreferenceRequest(
        @Size(max = 30) String theme,
        Boolean sidebarCollapsed,
        Boolean showPrivateFirst
) {
}
