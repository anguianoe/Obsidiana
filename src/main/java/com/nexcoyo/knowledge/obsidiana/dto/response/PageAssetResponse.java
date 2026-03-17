package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.PageAssetRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PageAssetResponse( UUID id, UUID pageId, UUID assetId, PageAssetRole assetRole, String displayName, Integer sortOrder, UUID createdBy, OffsetDateTime createdAt) {}
