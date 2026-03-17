package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageHierarchyRepository extends JpaRepository<PageHierarchy, UUID> {
    List< PageHierarchy > findAllByWorkspaceIdAndParentPageIdOrderBySortOrderAsc( UUID workspaceId, UUID parentPageId);
    List<PageHierarchy> findAllByWorkspaceIdAndChildPageId(UUID workspaceId, UUID childPageId);
    Optional<PageHierarchy> findByWorkspaceIdAndParentPageIdAndChildPageId(UUID workspaceId, UUID parentPageId, UUID childPageId);
}
