package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RestoreAuditResponse(
        UUID id, UUID trashRecordId, TrashEntityType entityType, UUID entityId, UUID restoredBy,
        String restoreReason, JsonNode restorePayload, Instant restoredAt
) {}
