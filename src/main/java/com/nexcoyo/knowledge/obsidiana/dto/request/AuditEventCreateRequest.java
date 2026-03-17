package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AuditEventCreateRequest(
    UUID id,
    @NotBlank String eventType,
    @NotBlank String entityType,
    UUID entityId,
    UUID actorUserId,
    UUID workspaceId,
    JsonNode eventPayload
) {}
