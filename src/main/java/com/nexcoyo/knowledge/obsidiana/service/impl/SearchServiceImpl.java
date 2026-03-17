package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.projection.CommentThreadProjection;
import com.nexcoyo.knowledge.obsidiana.repository.PageCommentRepository;
import com.nexcoyo.knowledge.obsidiana.repository.StoredAssetRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.service.SearchService;
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
public class SearchServiceImpl implements SearchService {

    private final WikiPageRepository wikiPageRepository;
    private final PageCommentRepository pageCommentRepository;
    private final StoredAssetRepository storedAssetRepository;

    @Override
    public Page< WikiPage > searchAccessiblePages( UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable) {
        return wikiPageRepository.searchAccessiblePages(userId, workspaceId, tagId, searchText, pageable);
    }

    @Override
    public List< CommentThreadProjection > loadCommentThread( UUID pageId, UUID workspaceId, UUID parentCommentId) {
        return pageCommentRepository.findThread(pageId, workspaceId, parentCommentId);
    }

    @Override
    public Page< StoredAsset > searchOrphanAssets( Pageable pageable) {
        return storedAssetRepository.findOrphanCandidates(pageable);
    }
}
