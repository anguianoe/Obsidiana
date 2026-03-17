package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageCommentReactionRepository extends JpaRepository< PageCommentReaction, UUID> {
    List<PageCommentReaction> findAllByCommentId(UUID commentId);
    Optional<PageCommentReaction> findByCommentIdAndUserId(UUID commentId, UUID userId);
}
