package com.nexcoyo.knowledge.obsidiana.service.dto.search;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.util.enums.CommentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentSearchCriteria {
    private UUID pageId;
    private UUID workspaceId;
    private UUID authorUserId;
    private UUID parentCommentId;
    private CommentStatus status;
    private String text;
    private Boolean onlyRoots;
}
