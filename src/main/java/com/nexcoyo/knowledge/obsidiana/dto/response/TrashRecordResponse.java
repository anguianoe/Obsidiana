package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;

import java.time.Instant;
import java.util.UUID;

public record TrashRecordResponse(
        UUID id, TrashEntityType entityType, UUID entityId, UUID workspaceId, UUID pageId, UUID assetId, UUID commentId, UUID deletedBy,
        String deleteReason, JsonNode snapshotPayload, Instant deletedAt, Instant restoreDeadlineAt,
        Instant restoredAt, Instant purgeScheduledAt, TrashStatus status
) {}
