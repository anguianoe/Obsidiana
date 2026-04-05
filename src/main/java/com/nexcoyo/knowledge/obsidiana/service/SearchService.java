package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.projection.CommentThreadProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    Page< WikiPage > searchAccessiblePages( UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable);
    List< CommentThreadProjection > loadCommentThread( UUID userId, UUID pageId, UUID workspaceId, UUID parentCommentId);
    List< CommentThreadProjection > loadCommentThread( UUID pageId, UUID workspaceId, UUID parentCommentId);
    Page< StoredAsset > searchOrphanAssets( UUID userId, Pageable pageable);
    Page< StoredAsset > searchOrphanAssets( Pageable pageable);
}
