package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditMode;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WikiPageResponse(
        UUID id, UUID publicUuid, UUID ownerUserId, String title, String slug, EditMode editMode, PageStatus pageStatus,
        Boolean isEncrypted, Boolean isPublicable, UUID currentRevisionId, Instant createdAt, Instant updatedAt
) {}
