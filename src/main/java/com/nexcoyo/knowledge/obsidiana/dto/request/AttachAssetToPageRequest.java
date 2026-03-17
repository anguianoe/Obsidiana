package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.PageAssetRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AttachAssetToPageRequest(
    @NotNull UUID assetId,
    @NotNull UUID pageId,
    @NotNull PageAssetRole role,
    UUID actorUserId,
    @Size(max = 255) String displayName,
    Integer sortOrder
) {}
