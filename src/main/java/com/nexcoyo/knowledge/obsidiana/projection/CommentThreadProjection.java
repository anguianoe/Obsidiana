package com.nexcoyo.knowledge.obsidiana.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface CommentThreadProjection {
    UUID getCommentId();
    UUID getParentCommentId();
    UUID getAuthorUserId();
    String getBody();
    OffsetDateTime getCreatedAt();
    Long getReactionCount();
    Long getReplyCount();
}
