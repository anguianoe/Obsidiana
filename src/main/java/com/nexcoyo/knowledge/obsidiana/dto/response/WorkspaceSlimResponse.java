package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.util.UUID;

public record WorkspaceSlimResponse(
        UUID id,
        String name,
        String slug,
        String kind,
        String status,
        String approvalStatus,
        String description
) {
}
