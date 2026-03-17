package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.UUID;
import java.util.List;

import com.nexcoyo.knowledge.obsidiana.entity.PublicPageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublicPageAssetRepository extends JpaRepository<PublicPageAsset, UUID>, JpaSpecificationExecutor<PublicPageAsset> {
    List< PublicPageAsset > findByPublicationId( UUID publicationId);

    List<PublicPageAsset> findByAssetId(UUID assetId);

}
