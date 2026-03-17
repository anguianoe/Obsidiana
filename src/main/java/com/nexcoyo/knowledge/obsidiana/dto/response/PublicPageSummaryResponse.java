package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PublicPageSummaryResponse(UUID publicationId, UUID pageId, UUID revisionId, String publicSlug, String publicTitle, OffsetDateTime publishedAt) {}
