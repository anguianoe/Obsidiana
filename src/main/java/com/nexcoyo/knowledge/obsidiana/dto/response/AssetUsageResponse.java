package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.util.UUID;

public record AssetUsageResponse(UUID assetId, Long pageLinks, Long revisionRefs, Long publicLinks) {}
