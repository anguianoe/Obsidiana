package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageRevisionCreateRequest;
import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.RevisionSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageRevisionViewResponse;
import com.nexcoyo.knowledge.obsidiana.facade.WikiPageRevisionFacade;
import com.nexcoyo.knowledge.obsidiana.facade.support.AccessContext;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/page-revisions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class WikiPageRevisionAdminController {
    private final WikiPageRevisionFacade revisionFacade;
    private final GeneralService generalService;

    @GetMapping("/{revisionId}")
    public WikiPageRevisionViewResponse getById(@PathVariable UUID revisionId) {
        UUID actorId = generalService.getIdUserFromSession();
        return revisionFacade.getById(revisionId, AccessContext.admin(actorId));
    }

    @GetMapping("/latest/{pageId}")
    public WikiPageRevisionViewResponse latest(@PathVariable UUID pageId) {
        UUID actorId = generalService.getIdUserFromSession();
        return revisionFacade.latest(pageId, AccessContext.admin(actorId));
    }

    @GetMapping("/summary/{pageId}")
    public PageResponse<RevisionSummaryResponse> summary(@PathVariable UUID pageId, @PageableDefault(size = 50) Pageable pageable) {
        UUID actorId = generalService.getIdUserFromSession();
        return revisionFacade.summary(pageId, AccessContext.admin(actorId), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WikiPageRevisionViewResponse create(@Valid @RequestBody WikiPageRevisionCreateRequest request) {
        UUID userId = generalService.getIdUserFromSession();

        // Product decision: CKEDITOR remains fixed until other editors are supported.
        return revisionFacade.save(new WikiPageRevisionCreateRequest(
                null, request.pageId(), null, request.titleSnapshot(), EditorType.CKEDITOR,
                request.contentHtml(), request.contentText(), request.contentCiphertext(), request.changeSummary(), request.isEncrypted(),
                request.contentIv(), request.contentAuthTag(), request.encryptionKdf(), request.isPinned(),
                userId, true
        ), AccessContext.admin(userId));
    }

    @PostMapping("/{revisionId}/new-version")
    @ResponseStatus(HttpStatus.CREATED)
    public WikiPageRevisionViewResponse newVersion(@PathVariable UUID revisionId, @Valid @RequestBody WikiPageRevisionCreateRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        // Product decision: CKEDITOR remains fixed until other editors are supported.
        return revisionFacade.save(new WikiPageRevisionCreateRequest(
                revisionId, request.pageId(), null, request.titleSnapshot(), EditorType.CKEDITOR,
                request.contentHtml(), request.contentText(), request.contentCiphertext(), request.changeSummary(), request.isEncrypted(),
                request.contentIv(), request.contentAuthTag(), request.encryptionKdf(), request.isPinned(),
                userId, true
        ), AccessContext.admin(userId));
    }

    @PostMapping("/restore/{revisionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WikiPageRevisionViewResponse restore(@PathVariable UUID revisionId) {
        UUID userId = generalService.getIdUserFromSession();
        return revisionFacade.restore(revisionId, AccessContext.admin(userId));
    }
}
