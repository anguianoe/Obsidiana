package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.projection.PageTreeNodeProjection;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageWorkspaceLinkRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WikiPageSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.service.specification.WikiPageSpecifications;
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
public class WikiPageServiceImpl implements WikiPageService {

    private final WikiPageRepository wikiPageRepository;
    private final PageWorkspaceLinkRepository pageWorkspaceLinkRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Page< WikiPage > search( WikiPageSearchCriteria criteria, Pageable pageable) {
        return wikiPageRepository.findAll(WikiPageSpecifications.byCriteria(criteria), pageable);
    }

    @Override
    public Page<WikiPage> searchAccessible(UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable) {
        return wikiPageRepository.searchAccessiblePages(userId, workspaceId, tagId, searchText, pageable);
    }

    @Override
    public WikiPage getRequired(UUID pageId) {
        return wikiPageRepository.findById(pageId)
            .orElseThrow(() -> new EntityNotFoundException("Wiki page not found: " + pageId));
    }

    @Override
    @Transactional
    public WikiPage save(WikiPage page) {
        return wikiPageRepository.save(page);
    }

    @Override
    @Transactional
    public PageWorkspaceLink linkToWorkspace( UUID pageId, UUID workspaceId, UUID linkedBy) {
        return pageWorkspaceLinkRepository.findByPageIdAndWorkspaceId(pageId, workspaceId)
            .orElseGet(() -> {
                WikiPage page = getRequired(pageId);
                Workspace workspace = workspaceRepository.findById(workspaceId)
                                                         .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
                AppUser actor = linkedBy == null ? null : appUserRepository.findById(linkedBy)
                                                                           .orElseThrow(() -> new EntityNotFoundException("User not found: " + linkedBy));
                PageWorkspaceLink link = new PageWorkspaceLink();
                link.setPage(page);
                link.setWorkspace(workspace);
                link.setLinkedBy(actor);
                link.setLinkedAt(OffsetDateTime.now());
                return pageWorkspaceLinkRepository.save(link);
            });
    }

    @Override
    public List< PageTreeNodeProjection > getTree( UUID workspaceId, UUID parentPageId) {
        return wikiPageRepository.findTreeNodes(workspaceId, parentPageId);
    }
}
