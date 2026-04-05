package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.projection.CommentThreadProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PageCommentRepository extends JpaRepository< PageComment, UUID>, JpaSpecificationExecutor<PageComment> {

    List<PageComment> findAllByPageIdAndWorkspaceIdOrderByCreatedAtAsc(UUID pageId, UUID workspaceId);

    @Query("""
        select c.id as commentId,
               c.parentComment.id as parentCommentId,
               c.authorUser.id as authorUserId,
               c.body as body,
               c.createdAt as createdAt,
               (select count(r.id) from PageCommentReaction r where r.comment.id = c.id) as reactionCount,
               (select count(rc.id) from PageComment rc where rc.parentComment.id = c.id) as replyCount
        from PageComment c
        where c.page.id = :pageId
          and c.workspace.id = :workspaceId
          and (:parentCommentId is null and c.parentComment is null or c.parentComment.id = :parentCommentId)
        order by c.createdAt asc
    """)
    List< CommentThreadProjection > findThread( @Param("pageId") UUID pageId, @Param("workspaceId") UUID workspaceId, @Param("parentCommentId") UUID parentCommentId);

    @Query("""
        select c.id as commentId,
               c.parentComment.id as parentCommentId,
               c.authorUser.id as authorUserId,
               c.body as body,
               c.createdAt as createdAt,
               (select count(r.id) from PageCommentReaction r where r.comment.id = c.id) as reactionCount,
               (select count(rc.id) from PageComment rc where rc.parentComment.id = c.id) as replyCount
        from PageComment c
        where c.page.id = :pageId
          and c.workspace.id = :workspaceId
          and (
            c.page.ownerUser.id = :userId
            or exists (
              select wm.id
              from WorkspaceMembership wm
              where wm.workspace.id = c.workspace.id
                and wm.user.id = :userId
                and wm.status = com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus.ACTIVE
            )
          )
          and (:parentCommentId is null and c.parentComment is null or c.parentComment.id = :parentCommentId)
        order by c.createdAt asc
    """)
    List<CommentThreadProjection> findThreadForUser(
        @Param("userId") UUID userId,
        @Param("pageId") UUID pageId,
        @Param("workspaceId") UUID workspaceId,
        @Param("parentCommentId") UUID parentCommentId
    );
}
