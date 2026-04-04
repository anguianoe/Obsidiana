package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UserTrashRecordCreateRequest(
    UUID id,
    @NotNull TrashEntityType entityType,
    @NotNull UUID entityId,
    UUID workspaceId,
    UUID pageId,
    UUID assetId,
    UUID commentId,
    @Size(max = 255) String deleteReason,
    JsonNode snapshotPayload,
    Instant restoreDeadlineAt,
    Instant purgeScheduledAt,
    @NotNull TrashStatus status
) {}

