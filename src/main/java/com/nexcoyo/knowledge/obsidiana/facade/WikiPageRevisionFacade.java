package com.nexcoyo.knowledge.obsidiana.facade;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.common.exception.ApiException;
import com.nexcoyo.knowledge.obsidiana.common.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageRevisionCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.RevisionSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageRevisionViewResponse;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.facade.support.AccessContext;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageService;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WikiPageRevisionFacade {

    private static final int MAX_SUMMARY_PAGE_SIZE = 200;

    private final WikiPageRevisionService revisionService;
    private final WikiPageService wikiPageService;
    private final EntityReferenceResolver refs;

    public WikiPageRevisionViewResponse getById(UUID revisionId, AccessContext accessContext) {
        WikiPageRevision revision = revisionService.getRequired(revisionId);
        if (accessContext.requiresPageAccessCheck()) {
            wikiPageService.assertUserPageAccess(revision.getPage().getId(), accessContext.actorUserId());
        }
        return ApiMapper.toResponse(revision);
    }

    public WikiPageRevisionViewResponse latest(UUID pageId, AccessContext accessContext) {
        if (accessContext.requiresPageAccessCheck()) {
            wikiPageService.assertUserPageAccess(pageId, accessContext.actorUserId());
        }
        return ApiMapper.toResponse(revisionService.getLatestRevision(pageId));
    }

    public PageResponse<RevisionSummaryResponse> summary(UUID pageId, AccessContext accessContext, Pageable pageable) {
        if (accessContext.requiresPageAccessCheck()) {
            wikiPageService.assertUserPageAccess(pageId, accessContext.actorUserId());
        }
        Sort sort = pageable.getSort().isSorted()
            ? pageable.getSort()
            : Sort.by(Sort.Direction.DESC, "revisionNumber");
        Pageable boundedPageable = PageRequest.of(
            pageable.getPageNumber(),
            Math.min(Math.max(pageable.getPageSize(), 1), MAX_SUMMARY_PAGE_SIZE),
            sort
        );
        return PageResponse.from(revisionService.getRevisionSummary(pageId, boundedPageable), ApiMapper::toResponse);
    }

    public WikiPageRevisionViewResponse save(WikiPageRevisionCreateRequest request, AccessContext accessContext) {
        if (accessContext.requiresPageAccessCheck()) {
            wikiPageService.assertUserPageAccess(request.pageId(), accessContext.actorUserId());
        }

        WikiPageRevision entity = request.id() == null ? new WikiPageRevision() : revisionService.getRequired(request.id());
        entity.setPage(refs.page(request.pageId()));
        entity.setRevisionNumber(request.revisionNumber());
        entity.setTitleSnapshot(request.titleSnapshot());
        entity.setEditorType(request.editorType());
        entity.setContentHtml(request.contentHtml());
        entity.setContentText(request.contentText());
        entity.setChangeSummary(request.changeSummary());
        entity.setIsEncrypted(request.isEncrypted());
        entity.setContentIv(request.contentIv());
        entity.setContentAuthTag(request.contentAuthTag());
        entity.setEncryptionKdf(request.encryptionKdf());
        entity.setIsPinned(request.isPinned());
        entity.setCreatedBy(refs.user(request.createdBy()));

        if (Boolean.TRUE.equals(request.isEncrypted())) {
            entity.setContentCiphertext(decodeCiphertext(request.contentCiphertext()));
            entity.setContentHtml(null);
            entity.setContentText(null);
        } else {
            entity.setContentCiphertext(null);
            entity.setContentIv(null);
            entity.setContentAuthTag(null);
            entity.setEncryptionKdf(null);
        }

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(OffsetDateTime.now());
        }
        return ApiMapper.toResponse(revisionService.saveRevision(entity, Boolean.TRUE.equals(request.updatePagePointer())));
    }

    private byte[] decodeCiphertext(String encodedCiphertext) {
        try {
            return Base64.getDecoder().decode(encodedCiphertext);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.REVISION_INVALID_CIPHERTEXT, "contentCiphertext must be valid Base64");
        }
    }

    public WikiPageRevisionViewResponse restore(UUID revisionId, AccessContext accessContext) {
        WikiPageRevision revision = revisionService.getRequired(revisionId);
        if (accessContext.requiresPageAccessCheck()) {
            wikiPageService.assertUserPageAccess(revision.getPage().getId(), accessContext.actorUserId());
        }
        return ApiMapper.toResponse(revisionService.restoreRevision(revisionId, accessContext.actorUserId()));
    }
}
