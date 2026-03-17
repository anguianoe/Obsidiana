package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageRevisionCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.RevisionSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageRevisionResponse;
import com.nexcoyo.knowledge.obsidiana.facade.WikiPageRevisionFacade;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/page-revisions")
@RequiredArgsConstructor
public class WikiPageRevisionController {

    private final WikiPageRevisionFacade revisionFacade;

    @GetMapping("/{revisionId}")
    public WikiPageRevisionResponse getById( @PathVariable UUID revisionId) {
        return revisionFacade.getById(revisionId);
    }

    @GetMapping("/latest/{pageId}")
    public WikiPageRevisionResponse latest(@PathVariable UUID pageId) {
        return revisionFacade.latest(pageId);
    }

    @GetMapping("/summary/{pageId}")
    public List< RevisionSummaryResponse > summary( @PathVariable UUID pageId) {
        return revisionFacade.summary(pageId);
    }

    @PostMapping
    public WikiPageRevisionResponse create(@Valid @RequestBody WikiPageRevisionCreateRequest request) {
        return revisionFacade.save(request);
    }

    @PutMapping("/{revisionId}")
    public WikiPageRevisionResponse update(@PathVariable UUID revisionId, @Valid @RequestBody WikiPageRevisionCreateRequest request) {
        return revisionFacade.save(new WikiPageRevisionCreateRequest(
            revisionId, request.pageId(), request.revisionNumber(), request.titleSnapshot(), request.editorType(),
            request.contentHtml(), request.contentText(), request.changeSummary(), request.isEncrypted(),
            request.contentIv(), request.contentAuthTag(), request.encryptionKdf(), request.isPinned(),
            request.createdBy(), request.updatePagePointer()
        ));
    }
}
