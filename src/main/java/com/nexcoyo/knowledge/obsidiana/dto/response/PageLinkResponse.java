package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageLinkResponse(UUID id, UUID pageId, UUID workspaceId, OffsetDateTime linkedAt, UUID linkedBy) {}
