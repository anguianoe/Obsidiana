package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageRevisionAssetRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRevisionAssetRefRepository extends JpaRepository<PageRevisionAssetRef, UUID> {
    List<PageRevisionAssetRef> findAllByRevisionId(UUID revisionId);
    List< PageRevisionAssetRef > findAllByAssetId( UUID assetId);
}
