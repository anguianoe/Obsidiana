package com.nexcoyo.knowledge.obsidiana.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PublicPageSummaryProjection {
    UUID getPublicationId();
    UUID getPageId();
    UUID getRevisionId();
    String getPublicSlug();
    String getPublicTitle();
    OffsetDateTime getPublishedAt();
}
