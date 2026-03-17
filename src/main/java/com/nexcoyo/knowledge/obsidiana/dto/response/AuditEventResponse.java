package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditEventResponse(UUID id, String eventType, String entityType, UUID entityId, UUID actorUserId, UUID workspaceId, JsonNode eventPayload, OffsetDateTime createdAt) {}
