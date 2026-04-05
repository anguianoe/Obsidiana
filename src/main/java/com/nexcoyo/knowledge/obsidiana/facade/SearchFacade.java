package com.nexcoyo.knowledge.obsidiana.facade;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.CommentThreadResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.StoredAssetResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageResponse;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchFacade {

    private static final int MAX_PAGE_SIZE = 200;

    private final SearchService searchService;

    public PageResponse< WikiPageResponse > accessiblePages( UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable) {
        return PageResponse.from(searchService.searchAccessiblePages(userId, workspaceId, tagId, searchText, sanitize(pageable)), ApiMapper::toResponse);
    }

    public List< CommentThreadResponse > commentThread( UUID userId, UUID pageId, UUID workspaceId, UUID parentCommentId) {
        return searchService.loadCommentThread(userId, pageId, workspaceId, parentCommentId).stream().map(ApiMapper::toResponse).toList();
    }

    public List< CommentThreadResponse > commentThread( UUID pageId, UUID workspaceId, UUID parentCommentId) {
        return searchService.loadCommentThread(pageId, workspaceId, parentCommentId).stream().map(ApiMapper::toResponse).toList();
    }

    public PageResponse< StoredAssetResponse > orphanAssets( UUID userId, Pageable pageable) {
        return PageResponse.from(searchService.searchOrphanAssets(userId, sanitize(pageable)), ApiMapper::toResponse);
    }

    public PageResponse< StoredAssetResponse > orphanAssets( Pageable pageable) {
        return PageResponse.from(searchService.searchOrphanAssets(sanitize(pageable)), ApiMapper::toResponse);
    }

    private Pageable sanitize( Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return pageable;
        }
        if (pageable.getPageSize() <= MAX_PAGE_SIZE) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
    }
}
