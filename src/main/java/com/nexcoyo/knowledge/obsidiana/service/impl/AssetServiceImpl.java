package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageAsset;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.projection.AssetUsageProjection;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageAssetRepository;
import com.nexcoyo.knowledge.obsidiana.repository.StoredAssetRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.service.AssetService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.AssetSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.service.specification.StoredAssetSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageAssetRole;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetServiceImpl implements AssetService {

    private final StoredAssetRepository storedAssetRepository;
    private final PageAssetRepository pageAssetRepository;
    private final WikiPageRepository wikiPageRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Page< StoredAsset > search( AssetSearchCriteria criteria, Pageable pageable) {
        return storedAssetRepository.findAll(StoredAssetSpecifications.byCriteria(criteria), pageable);
    }

    @Override
    public StoredAsset getRequired(UUID assetId) {
        return storedAssetRepository.findById(assetId)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + assetId));
    }

    @Override
    @Transactional
    public StoredAsset save(StoredAsset asset) {
        return storedAssetRepository.save(asset);
    }

    @Override
    @Transactional
    public PageAsset attachToPage( UUID assetId, UUID pageId, PageAssetRole role, UUID actorUserId, String displayName, Integer sortOrder) {
        return pageAssetRepository.findByPageIdAndAssetIdAndAssetRole(pageId, assetId, role)
            .orElseGet(() -> {
                StoredAsset asset = getRequired(assetId);
                WikiPage page = wikiPageRepository.findById(pageId)
                                                  .orElseThrow(() -> new EntityNotFoundException("Wiki page not found: " + pageId));
                AppUser actor = actorUserId == null ? null : appUserRepository.findById(actorUserId)
                                                                              .orElseThrow(() -> new EntityNotFoundException("User not found: " + actorUserId));
                PageAsset pageAsset = new PageAsset();
                pageAsset.setAsset(asset);
                pageAsset.setPage(page);
                pageAsset.setAssetRole(role);
                pageAsset.setDisplayName(displayName);
                pageAsset.setSortOrder(sortOrder == null ? 0 : sortOrder);
                pageAsset.setCreatedBy(actor);
                pageAsset.setCreatedAt(OffsetDateTime.now());
                return pageAssetRepository.save(pageAsset);
            });
    }

    @Override
    public AssetUsageProjection getUsage( UUID assetId) {
        return storedAssetRepository.findUsage(assetId)
            .orElseThrow(() -> new EntityNotFoundException("Asset usage not found: " + assetId));
    }

    @Override
    public Page<StoredAsset> findOrphanCandidates(Pageable pageable) {
        return storedAssetRepository.findOrphanCandidates(pageable);
    }

    @Override
    public List<PageAsset> getPageAssets(UUID pageId) {
        return pageAssetRepository.findAllByPageIdOrderBySortOrderAsc(pageId);
    }
}
