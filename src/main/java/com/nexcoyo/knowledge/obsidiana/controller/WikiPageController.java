package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.LinkPageToWorkspaceRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageLinkResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTreeNodeResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageResponse;
import com.nexcoyo.knowledge.obsidiana.facade.WikiPageFacade;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class WikiPageController {

    private final WikiPageFacade wikiPageFacade;

    @GetMapping
    public PageResponse< WikiPageResponse > search(
        @RequestParam(required = false) String text,
        @RequestParam(required = false) UUID ownerUserId,
        @RequestParam(required = false) Boolean encrypted,
        @RequestParam(required = false) PageStatus status,
        Pageable pageable
    ) {
        return wikiPageFacade.search(text, ownerUserId, encrypted, status, pageable);
    }

    @GetMapping("/accessible")
    public PageResponse<WikiPageResponse> searchAccessible(
        @RequestParam UUID userId,
        @RequestParam(required = false) UUID workspaceId,
        @RequestParam(required = false) UUID tagId,
        @RequestParam(required = false) String searchText,
        Pageable pageable
    ) {
        return wikiPageFacade.searchAccessible(userId, workspaceId, tagId, searchText, pageable);
    }

    @GetMapping("/{pageId}")
    public WikiPageResponse getById(@PathVariable UUID pageId) {
        return wikiPageFacade.getById(pageId);
    }

    @PostMapping
    public WikiPageResponse create(@Valid @RequestBody WikiPageUpsertRequest request) {
        return wikiPageFacade.save(request);
    }

    @PutMapping("/{pageId}")
    public WikiPageResponse update(@PathVariable UUID pageId, @Valid @RequestBody WikiPageUpsertRequest request) {
        return wikiPageFacade.save(new WikiPageUpsertRequest(
            pageId, request.publicUuid(), request.ownerUserId(), request.title(), request.slug(),
            request.editMode(), request.pageStatus(), request.isEncrypted(), request.isPublicable(), request.currentRevisionId()
        ));
    }

    @PostMapping("/link-workspace")
    public PageLinkResponse linkToWorkspace( @Valid @RequestBody LinkPageToWorkspaceRequest request) {
        return wikiPageFacade.linkToWorkspace(request);
    }

    @GetMapping("/tree")
    public List< PageTreeNodeResponse > tree( @RequestParam UUID workspaceId, @RequestParam(required = false) UUID parentPageId) {
        return wikiPageFacade.tree(workspaceId, parentPageId);
    }
}
