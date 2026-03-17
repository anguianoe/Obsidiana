package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TrashRecordCreateRequest(
    UUID id,
    @NotNull TrashEntityType entityType,
    @NotNull UUID entityId,
    UUID workspaceId,
    UUID pageId,
    UUID assetId,
    UUID commentId,
    UUID deletedBy,
    @Size(max = 255) String deleteReason,
    JsonNode snapshotPayload,
    OffsetDateTime restoreDeadlineAt,
    OffsetDateTime purgeScheduledAt,
    @NotNull TrashStatus status
) {}
