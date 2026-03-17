package com.nexcoyo.knowledge.obsidiana.controller;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.CommentThreadResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.StoredAssetResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageResponse;
import com.nexcoyo.knowledge.obsidiana.facade.SearchFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchFacade searchFacade;

    @GetMapping("/pages/accessible")
    public PageResponse< WikiPageResponse > accessiblePages(
        @RequestParam UUID userId,
        @RequestParam(required = false) UUID workspaceId,
        @RequestParam(required = false) UUID tagId,
        @RequestParam(required = false) String searchText,
        Pageable pageable
    ) {
        return searchFacade.accessiblePages(userId, workspaceId, tagId, searchText, pageable);
    }

    @GetMapping("/comments/thread")
    public List< CommentThreadResponse > commentThread(
        @RequestParam UUID pageId,
        @RequestParam UUID workspaceId,
        @RequestParam(required = false) UUID parentCommentId
    ) {
        return searchFacade.commentThread(pageId, workspaceId, parentCommentId);
    }

    @GetMapping("/assets/orphans")
    public PageResponse< StoredAssetResponse > orphanAssets( Pageable pageable) {
        return searchFacade.orphanAssets(pageable);
    }
}
