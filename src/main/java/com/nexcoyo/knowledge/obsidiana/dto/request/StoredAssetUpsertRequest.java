package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StoredAssetUpsertRequest(
    UUID id,
    @NotBlank @Size(max = 30) String storageProvider,
    @NotBlank @Size(max = 255) String bucketName,
    @NotBlank String objectKey,
    @NotBlank @Size(max = 255) String originalFilename,
    @Size(max = 255) String normalizedFilename,
    @NotBlank @Size(max = 180) String mimeType,
    @NotNull AssetType assetType,
    @Size(max = 30) String fileExtension,
    @NotNull Long sizeBytes,
    @Size(max = 64) String checksumSha256,
    @NotNull AssetStatus status,
    UUID uploadedBy
) {}
