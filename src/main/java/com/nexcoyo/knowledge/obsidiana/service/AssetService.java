package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageAsset;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.projection.AssetUsageProjection;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.AssetSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageAssetRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssetService {
    Page< StoredAsset > search( AssetSearchCriteria criteria, Pageable pageable);
    StoredAsset getRequired(UUID assetId);
    StoredAsset save(StoredAsset asset);
    PageAsset attachToPage( UUID assetId, UUID pageId, PageAssetRole role, UUID actorUserId, String displayName, Integer sortOrder);
    AssetUsageProjection getUsage( UUID assetId);
    Page<StoredAsset> findOrphanCandidates(Pageable pageable);
    List< PageAsset > getPageAssets( UUID pageId);
}
