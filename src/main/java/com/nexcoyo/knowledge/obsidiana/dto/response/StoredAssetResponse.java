package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StoredAssetResponse(
        UUID id, String storageProvider, String bucketName, String objectKey, String originalFilename, String normalizedFilename,
        String mimeType, AssetType assetType, String fileExtension, Long sizeBytes, String checksumSha256, AssetStatus status,
        UUID uploadedBy, Instant createdAt, Instant updatedAt
) {}
