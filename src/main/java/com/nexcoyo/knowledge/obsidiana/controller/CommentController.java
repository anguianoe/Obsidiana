package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.PageCommentUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.ReactToCommentRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.CommentThreadResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageCommentReactionResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageCommentResponse;
import com.nexcoyo.knowledge.obsidiana.facade.CommentFacade;
import com.nexcoyo.knowledge.obsidiana.util.enums.CommentStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentFacade commentFacade;

    @GetMapping
    public PageResponse< PageCommentResponse > search(
        @RequestParam(required = false) UUID pageId,
        @RequestParam(required = false) UUID workspaceId,
        @RequestParam(required = false) UUID authorUserId,
        @RequestParam(required = false) CommentStatus status,
        Pageable pageable
    ) {
        return commentFacade.search(pageId, workspaceId, authorUserId, status, pageable);
    }

    @GetMapping("/thread")
    public List< CommentThreadResponse > thread(
        @RequestParam UUID pageId,
        @RequestParam UUID workspaceId,
        @RequestParam(required = false) UUID parentCommentId
    ) {
        return commentFacade.thread(pageId, workspaceId, parentCommentId);
    }

    @PostMapping
    public PageCommentResponse create(@Valid @RequestBody PageCommentUpsertRequest request) {
        return commentFacade.save(request);
    }

    @PutMapping("/{commentId}")
    public PageCommentResponse update(@PathVariable UUID commentId, @Valid @RequestBody PageCommentUpsertRequest request) {
        return commentFacade.save(new PageCommentUpsertRequest(
            commentId, request.pageId(), request.workspaceId(), request.authorUserId(),
            request.parentCommentId(), request.body(), request.commentStatus()
        ));
    }

    @PostMapping("/react")
    public PageCommentReactionResponse react( @Valid @RequestBody ReactToCommentRequest request) {
        return commentFacade.react(request);
    }
}
