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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchService searchService;

    public PageResponse< WikiPageResponse > accessiblePages( UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable) {
        return PageResponse.from(searchService.searchAccessiblePages(userId, workspaceId, tagId, searchText, pageable), ApiMapper::toResponse);
    }

    public List< CommentThreadResponse > commentThread( UUID pageId, UUID workspaceId, UUID parentCommentId) {
        return searchService.loadCommentThread(pageId, workspaceId, parentCommentId).stream().map(ApiMapper::toResponse).toList();
    }

    public PageResponse< StoredAssetResponse > orphanAssets( Pageable pageable) {
        return PageResponse.from(searchService.searchOrphanAssets(pageable), ApiMapper::toResponse);
    }
}
