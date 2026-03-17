package com.nexcoyo.knowledge.obsidiana.projection;

import java.util.UUID;

public interface AssetUsageProjection {
    UUID getAssetId();
    Long getPageLinks();
    Long getRevisionRefs();
    Long getPublicLinks();
}
