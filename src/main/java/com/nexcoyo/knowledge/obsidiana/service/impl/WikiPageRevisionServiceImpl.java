package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.common.exception.ApiException;
import com.nexcoyo.knowledge.obsidiana.common.exception.ErrorCode;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.projection.RevisionSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRevisionRepository;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageRevisionService;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WikiPageRevisionServiceImpl implements WikiPageRevisionService {

    private final WikiPageRevisionRepository wikiPageRevisionRepository;
    private final WikiPageRepository wikiPageRepository;
    private final EntityReferenceResolver refs;

    @Override
    public WikiPageRevision getRequired( UUID revisionId) {
        return wikiPageRevisionRepository.findViewById(revisionId)
            .orElseThrow(() -> new EntityNotFoundException("Revision not found: " + revisionId));
    }

    @Override
    public WikiPageRevision getLatestRevision(UUID pageId) {
        return wikiPageRevisionRepository.findTopByPageIdOrderByRevisionNumberDesc(pageId)
            .orElseThrow(() -> new EntityNotFoundException("No revisions found for page: " + pageId));
    }

    @Override
    public Page<RevisionSummaryProjection> getRevisionSummary(UUID pageId, Pageable pageable) {
        return wikiPageRevisionRepository.findRevisionSummaryByPageId(pageId, pageable);
    }

    @Override
    @Transactional
    public WikiPageRevision saveRevision(WikiPageRevision revision, boolean updatePagePointer) {
        UUID pageId = revision.getPage() == null ? null : revision.getPage().getId();
        if (pageId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.REVISION_PAGE_REQUIRED, "Revision page is required");
        }
        WikiPage page = wikiPageRepository.findById(pageId)
            .orElseThrow(() -> new EntityNotFoundException("Wiki page not found: " + pageId));
        Integer latestRevisionNumber = wikiPageRevisionRepository.findMaxRevisionNumberByPageId(pageId);

        if (revision.getId() != null) {
            WikiPageRevision persisted = getRequired(revision.getId());
            if (!persisted.getPage().getId().equals(pageId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.REVISION_PAGE_MISMATCH, "Revision does not belong to the target page");
            }
            boolean isOlderThanLatest = persisted.getRevisionNumber() < latestRevisionNumber;
            if (isOlderThanLatest && !updatePagePointer) {
                throw new ApiException(HttpStatus.CONFLICT, ErrorCode.REVISION_UPDATE_CONFLICT, "Cannot modify an old revision without creating a new version");
            }

            // Keep the same revision number when editing the current revision without moving the pointer.
            if (!updatePagePointer) {
                revision.setRevisionNumber(persisted.getRevisionNumber());
            }

            // Any update that moves the pointer creates a brand-new next version.
            if (updatePagePointer) {
                revision.setId(null);
                revision.setRevisionNumber(latestRevisionNumber + 1);
                revision.setCreatedAt(OffsetDateTime.now());
            }
        } else if (updatePagePointer) {
            revision.setRevisionNumber(latestRevisionNumber + 1);
        }

        revision.setPage(page);

        WikiPageRevision saved = wikiPageRevisionRepository.save(revision);
        if (updatePagePointer) {
            page.setCurrentRevision(saved);
            wikiPageRepository.save(page);
        }
        return saved;
    }

    @Override
    @Transactional
    public WikiPageRevision restoreRevision(UUID revisionId, UUID actorUserId) {
        WikiPageRevision source = getRequired(revisionId);
        UUID pageId = source.getPage().getId();

        WikiPageRevision restored = new WikiPageRevision();
        restored.setPage(refs.page(pageId));
        restored.setRevisionNumber(source.getRevisionNumber());
        restored.setTitleSnapshot(source.getTitleSnapshot());
        restored.setEditorType(source.getEditorType());
        restored.setContentHtml(source.getContentHtml());
        restored.setContentText(source.getContentText());
        restored.setChangeSummary("Restored from revision #" + source.getRevisionNumber());
        restored.setIsEncrypted(source.getIsEncrypted());
        restored.setContentCiphertext(source.getContentCiphertext());
        restored.setContentIv(source.getContentIv());
        restored.setContentAuthTag(source.getContentAuthTag());
        restored.setEncryptionKdf(source.getEncryptionKdf());
        restored.setIsPinned(source.getIsPinned());
        restored.setCreatedBy(refs.user(actorUserId));
        restored.setCreatedAt(OffsetDateTime.now());
        return saveRevision(restored, true);
    }
}
