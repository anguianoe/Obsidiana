package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.AttachAssetToPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.StoredAssetUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.AssetUsageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageAssetResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.StoredAssetResponse;
import com.nexcoyo.knowledge.obsidiana.facade.AssetFacade;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetType;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetFacade assetFacade;

    @GetMapping
    public PageResponse< StoredAssetResponse > search(
        @RequestParam(required = false) String text,
        @RequestParam(required = false) AssetType assetType,
        @RequestParam(required = false) AssetStatus status,
        @RequestParam(required = false) UUID uploadedBy,
        Pageable pageable
    ) {
        return assetFacade.search(text, assetType, status, uploadedBy, pageable);
    }

    @GetMapping("/{assetId}")
    public StoredAssetResponse getById(@PathVariable UUID assetId) {
        return assetFacade.getById(assetId);
    }

    @PostMapping
    public StoredAssetResponse create(@Valid @RequestBody StoredAssetUpsertRequest request) {
        return assetFacade.save(request);
    }

    @PutMapping("/{assetId}")
    public StoredAssetResponse update(@PathVariable UUID assetId, @Valid @RequestBody StoredAssetUpsertRequest request) {
        return assetFacade.save(new StoredAssetUpsertRequest(
            assetId, request.storageProvider(), request.bucketName(), request.objectKey(), request.originalFilename(),
            request.normalizedFilename(), request.mimeType(), request.assetType(), request.fileExtension(),
            request.sizeBytes(), request.checksumSha256(), request.status(), request.uploadedBy()
        ));
    }

    @PostMapping("/attach-to-page")
    public PageAssetResponse attachToPage( @Valid @RequestBody AttachAssetToPageRequest request) {
        return assetFacade.attachToPage(request);
    }

    @GetMapping("/{assetId}/usage")
    public AssetUsageResponse usage( @PathVariable UUID assetId) {
        return assetFacade.usage(assetId);
    }

    @GetMapping("/orphans")
    public PageResponse<StoredAssetResponse> orphanCandidates(Pageable pageable) {
        return assetFacade.orphanCandidates(pageable);
    }

    @GetMapping("/page/{pageId}")
    public List<PageAssetResponse> pageAssets(@PathVariable UUID pageId) {
        return assetFacade.pageAssets(pageId);
    }
}
