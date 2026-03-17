package com.nexcoyo.knowledge.obsidiana.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSearchRequest(
        String text,
        String email,
        String username,
        String systemRole,
        String status,
        UUID workspaceId,
        UUID tagId,
        OffsetDateTime createdFrom,
        OffsetDateTime createdTo,
        Boolean includeDeleted,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) {
}
