package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.entity.PageCommentReaction;
import com.nexcoyo.knowledge.obsidiana.projection.CommentThreadProjection;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageCommentReactionRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageCommentRepository;
import com.nexcoyo.knowledge.obsidiana.service.CommentService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.CommentSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.service.specification.PageCommentSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.ReactionType;
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
public class CommentServiceImpl implements CommentService {

    private final PageCommentRepository pageCommentRepository;
    private final PageCommentReactionRepository pageCommentReactionRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Page< PageComment > search( CommentSearchCriteria criteria, Pageable pageable) {
        return pageCommentRepository.findAll(PageCommentSpecifications.byCriteria(criteria), pageable);
    }

    @Override
    @Transactional
    public PageComment save(PageComment comment) {
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(OffsetDateTime.now());
        }
        return pageCommentRepository.save(comment);
    }

    @Override
    public List< CommentThreadProjection > getThread( UUID pageId, UUID workspaceId, UUID parentCommentId) {
        return pageCommentRepository.findThread(pageId, workspaceId, parentCommentId);
    }

    @Override
    @Transactional
    public PageCommentReaction react( UUID commentId, UUID userId, ReactionType reactionType) {
        PageComment comment = pageCommentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));
        AppUser user = appUserRepository.findById(userId)
                                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        PageCommentReaction reaction = pageCommentReactionRepository.findByCommentIdAndUserId(commentId, userId)
            .orElseGet(PageCommentReaction::new);
        reaction.setComment(comment);
        reaction.setUser(user);
        reaction.setReactionType(reactionType);
        if (reaction.getCreatedAt() == null) {
            reaction.setCreatedAt(OffsetDateTime.now());
        }
        return pageCommentReactionRepository.save(reaction);
    }
}
