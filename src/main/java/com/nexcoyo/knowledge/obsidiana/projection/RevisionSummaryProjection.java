package com.nexcoyo.knowledge.obsidiana.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface RevisionSummaryProjection {
    UUID getRevisionId();
    Integer getRevisionNumber();
    String getTitleSnapshot();
    Boolean getPinned();
    UUID getCreatedBy();
    OffsetDateTime getCreatedAt();
}
