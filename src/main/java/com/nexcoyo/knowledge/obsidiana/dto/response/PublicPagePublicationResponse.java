package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PublicPagePublicationResponse(
        UUID id, UUID pageId, UUID revisionId, String publicSlug, String publicTitle, String publicHtml,
        PublicationStatus publicationStatus, UUID publishedBy, OffsetDateTime publishedAt, OffsetDateTime unpublishedAt
) {}
