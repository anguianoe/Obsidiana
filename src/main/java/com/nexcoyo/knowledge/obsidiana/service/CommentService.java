package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.entity.PageCommentReaction;
import com.nexcoyo.knowledge.obsidiana.projection.CommentThreadProjection;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.CommentSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    Page<PageComment> search( CommentSearchCriteria criteria, Pageable pageable);
    PageComment save( PageComment comment);
    List< CommentThreadProjection > getThread( UUID pageId, UUID workspaceId, UUID parentCommentId);
    PageCommentReaction react( UUID commentId, UUID userId, ReactionType reactionType);
}
