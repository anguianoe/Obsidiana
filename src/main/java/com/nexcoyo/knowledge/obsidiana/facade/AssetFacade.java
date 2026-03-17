package com.nexcoyo.knowledge.obsidiana.facade;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.AttachAssetToPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.StoredAssetUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.AssetUsageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageAssetResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.StoredAssetResponse;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.AssetService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.AssetSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssetFacade {

    private final AssetService assetService;
    private final EntityReferenceResolver refs;

    public PageResponse< StoredAssetResponse > search( String text, AssetType assetType, AssetStatus status, UUID uploadedBy, Pageable pageable) {
        AssetSearchCriteria criteria = new AssetSearchCriteria();
        criteria.setFileNameOrObjectKey(text);
        criteria.setAssetType(assetType);
        criteria.setStatus(status);
        criteria.setUploadedBy(uploadedBy);
        return PageResponse.from(assetService.search(criteria, pageable), ApiMapper::toResponse);
    }

    public StoredAssetResponse getById(UUID id) {
        return ApiMapper.toResponse(assetService.getRequired(id));
    }

    public StoredAssetResponse save( StoredAssetUpsertRequest request) {
        StoredAsset entity = request.id() == null ? new StoredAsset() : assetService.getRequired(request.id());
        entity.setStorageProvider(request.storageProvider());
        entity.setBucketName(request.bucketName());
        entity.setObjectKey(request.objectKey());
        entity.setOriginalFilename(request.originalFilename());
        entity.setNormalizedFilename(request.normalizedFilename());
        entity.setMimeType(request.mimeType());
        entity.setAssetType(request.assetType());
        entity.setFileExtension(request.fileExtension());
        entity.setSizeBytes(request.sizeBytes());
        entity.setChecksumSha256(request.checksumSha256());
        entity.setStatus(request.status());
        entity.setUploadedBy(refs.user(request.uploadedBy()));
        return ApiMapper.toResponse(assetService.save(entity));
    }

    public PageAssetResponse attachToPage( AttachAssetToPageRequest request) {
        return ApiMapper.toResponse(assetService.attachToPage(
            request.assetId(),
            request.pageId(),
            request.role(),
            request.actorUserId(),
            request.displayName(),
            request.sortOrder()
        ));
    }

    public AssetUsageResponse usage( UUID assetId) {
        return ApiMapper.toResponse(assetService.getUsage(assetId));
    }

    public PageResponse<StoredAssetResponse> orphanCandidates(Pageable pageable) {
        return PageResponse.from(assetService.findOrphanCandidates(pageable), ApiMapper::toResponse);
    }

    public List<PageAssetResponse> pageAssets(UUID pageId) {
        return assetService.getPageAssets(pageId).stream().map(ApiMapper::toResponse).toList();
    }
}
