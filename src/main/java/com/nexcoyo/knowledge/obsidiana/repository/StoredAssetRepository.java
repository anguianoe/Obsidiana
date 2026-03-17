package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.projection.AssetUsageProjection;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoredAssetRepository extends JpaRepository< StoredAsset, UUID>, JpaSpecificationExecutor<StoredAsset> {

    Optional<StoredAsset> findByBucketNameAndObjectKey(String bucketName, String objectKey);

    Page<StoredAsset> findAllByStatus( AssetStatus status, Pageable pageable);

    @Query("""
        select a.id as assetId,
               (select count(pa.id) from PageAsset pa where pa.asset.id = a.id) as pageLinks,
               (select count(pr.id) from PageRevisionAssetRef pr where pr.asset.id = a.id) as revisionRefs,
               (select count(ppa.id) from PublicPageAsset ppa where ppa.asset.id = a.id) as publicLinks
        from StoredAsset a
        where a.id = :assetId
    """)
    Optional< AssetUsageProjection > findUsage( @Param("assetId") UUID assetId);

    @Query("""
        select a
        from StoredAsset a
        where a.status = com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus.ACTIVE
          and not exists (select 1 from PageAsset pa where pa.asset.id = a.id)
          and not exists (select 1 from PageRevisionAssetRef pr where pr.asset.id = a.id)
          and not exists (select 1 from PublicPageAsset ppa where ppa.asset.id = a.id)
        order by a.createdAt asc
    """)
    Page<StoredAsset> findOrphanCandidates(Pageable pageable);
}
