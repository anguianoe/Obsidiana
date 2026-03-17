package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.PageCommentUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.ReactToCommentRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.CommentThreadResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageCommentReactionResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageCommentResponse;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.CommentService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.CommentSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.CommentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentFacade {

    private final CommentService commentService;
    private final EntityReferenceResolver refs;

    public PageResponse< PageCommentResponse > search( UUID pageId, UUID workspaceId, UUID authorUserId, CommentStatus status, Pageable pageable) {
        CommentSearchCriteria criteria = new CommentSearchCriteria();
        criteria.setPageId(pageId);
        criteria.setWorkspaceId(workspaceId);
        criteria.setAuthorUserId(authorUserId);
        criteria.setStatus(status);
        return PageResponse.from(commentService.search(criteria, pageable), ApiMapper::toResponse);
    }

    public PageCommentResponse save( PageCommentUpsertRequest request) {
        PageComment entity = new PageComment();
        if (request.id() != null) {
            entity.setId(request.id());
        }
        entity.setPage(refs.page(request.pageId()));
        entity.setWorkspace(refs.workspace(request.workspaceId()));
        entity.setAuthorUser(refs.user(request.authorUserId()));
        entity.setParentComment(refs.comment(request.parentCommentId()));
        entity.setBody(request.body());
        entity.setCommentStatus(request.commentStatus());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(OffsetDateTime.now());
        }
        return ApiMapper.toResponse(commentService.save(entity));
    }

    public List< CommentThreadResponse > thread( UUID pageId, UUID workspaceId, UUID parentCommentId) {
        return commentService.getThread(pageId, workspaceId, parentCommentId).stream().map(ApiMapper::toResponse).toList();
    }

    public PageCommentReactionResponse react( ReactToCommentRequest request) {
        return ApiMapper.toResponse(commentService.react(request.commentId(), request.userId(), request.reactionType()));
    }
}
