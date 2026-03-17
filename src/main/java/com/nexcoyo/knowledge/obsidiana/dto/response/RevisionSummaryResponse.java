package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RevisionSummaryResponse(UUID revisionId, Integer revisionNumber, String titleSnapshot, Boolean pinned, UUID createdBy, OffsetDateTime createdAt) {}
