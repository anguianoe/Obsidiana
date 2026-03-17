package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.projection.PageTreeNodeProjection;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WikiPageSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WikiPageService {
    Page< WikiPage > search( WikiPageSearchCriteria criteria, Pageable pageable);
    Page<WikiPage> searchAccessible(UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable);
    WikiPage getRequired(UUID pageId);
    WikiPage save(WikiPage page);
    PageWorkspaceLink linkToWorkspace( UUID pageId, UUID workspaceId, UUID linkedBy);
    List< PageTreeNodeProjection > getTree( UUID workspaceId, UUID parentPageId);
}
