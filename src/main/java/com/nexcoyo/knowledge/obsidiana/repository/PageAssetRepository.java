package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageAsset;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageAssetRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageAssetRepository extends JpaRepository<PageAsset, UUID> {
    List<PageAsset> findAllByPageIdOrderBySortOrderAsc(UUID pageId);
    List<PageAsset> findAllByAssetId(UUID assetId);
    Optional< PageAsset > findByPageIdAndAssetIdAndAssetRole( UUID pageId, UUID assetId, PageAssetRole assetRole);
}
