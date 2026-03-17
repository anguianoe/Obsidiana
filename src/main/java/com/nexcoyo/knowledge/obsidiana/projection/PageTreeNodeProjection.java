package com.nexcoyo.knowledge.obsidiana.projection;

import java.util.UUID;

public interface PageTreeNodeProjection {
    UUID getPageId();
    String getTitle();
    String getSlug();
    Integer getSortOrder();
    Long getChildCount();
}
