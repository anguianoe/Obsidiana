package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageWorkspaceLinkRepository extends JpaRepository<PageWorkspaceLink, UUID> {
    Optional< PageWorkspaceLink > findByPageIdAndWorkspaceId( UUID pageId, UUID workspaceId);
    List<PageWorkspaceLink> findAllByPageId(UUID pageId);
    List<PageWorkspaceLink> findAllByWorkspaceId(UUID workspaceId);
    boolean existsByPageIdAndWorkspaceId(UUID pageId, UUID workspaceId);
}
