package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.common.exception.ApiException;
import com.nexcoyo.knowledge.obsidiana.common.exception.ErrorCode;
import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.projection.PublicPageSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.repository.PublicPagePublicationRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRevisionRepository;
import com.nexcoyo.knowledge.obsidiana.service.PublicationService;
import com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus;
import jakarta.persistence.EntityNotFoundException;
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
public class PublicationServiceImpl implements PublicationService {

    private final PublicPagePublicationRepository publicPagePublicationRepository;
    private final WikiPageRepository wikiPageRepository;
    private final WikiPageRevisionRepository wikiPageRevisionRepository;

    @Override
    public PublicPagePublication getLiveBySlug( String publicSlug) {
        return publicPagePublicationRepository.findByPublicSlugAndPublicationStatus(publicSlug, PublicationStatus.LIVE)
            .orElseThrow(() -> new EntityNotFoundException("Live publication not found for slug: " + publicSlug));
    }

    @Override
    public PublicPagePublication getLiveByPageId(UUID pageId) {
        return publicPagePublicationRepository.findByPageIdAndPublicationStatus(pageId, PublicationStatus.LIVE)
            .orElseThrow(() -> new EntityNotFoundException("Live publication not found for page: " + pageId));
    }

    @Override
    @Transactional
    public PublicPagePublication publishForUser(PublicPagePublication publication, UUID userId) {
        UUID pageId = publication.getPage().getId();
        if (!wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "You do not have access to publish this page");
        }
        validateRevisionBelongsToPage(publication);
        return publicPagePublicationRepository.save(publication);
    }

    @Override
    @Transactional
    public PublicPagePublication publish(PublicPagePublication publication) {
        validateRevisionBelongsToPage(publication);
        return publicPagePublicationRepository.save(publication);
    }

    @Override
    public Page< PublicPageSummaryProjection > getLiveSummaries( Pageable pageable) {
        return publicPagePublicationRepository.findLivePublicationSummaries(pageable);
    }

    private void validateRevisionBelongsToPage( PublicPagePublication publication) {
        UUID revisionId = publication.getRevision().getId();
        UUID pageId = publication.getPage().getId();
        if (!wikiPageRevisionRepository.existsByIdAndPageId(revisionId, pageId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.REVISION_PAGE_MISMATCH, "Revision does not belong to page");
        }
    }
}
