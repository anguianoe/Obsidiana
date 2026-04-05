package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.PublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserPublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPagePublicationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPageSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicationFacade {

    private static final int MAX_PAGE_SIZE = 200;

    private final PublicationService publicationService;
    private final EntityReferenceResolver refs;

    public PublicPagePublicationResponse getLiveBySlug( String slug) {
        return ApiMapper.toResponse(publicationService.getLiveBySlug(slug));
    }

    public PublicPagePublicationResponse getLiveByPageId(UUID pageId) {
        return ApiMapper.toResponse(publicationService.getLiveByPageId(pageId));
    }

    public PageResponse< PublicPageSummaryResponse > liveSummaries( Pageable pageable) {
        return PageResponse.from(publicationService.getLiveSummaries(sanitize(pageable)), ApiMapper::toResponse);
    }

    public PublicPagePublicationResponse publishForUser( UserPublishPageRequest request, UUID userId) {
        PublicPagePublication entity = new PublicPagePublication();
        if (request.id() != null) {
            entity.setId(request.id());
        }
        entity.setPage(refs.page(request.pageId()));
        entity.setRevision(refs.revision(request.revisionId()));
        entity.setPublicSlug(request.publicSlug());
        entity.setPublicTitle(request.publicTitle());
        entity.setPublicHtml(request.publicHtml());
        entity.setPublicationStatus(request.publicationStatus());
        entity.setPublishedBy(refs.user(userId));
        entity.setPublishedAt(OffsetDateTime.now());
        return ApiMapper.toResponse(publicationService.publishForUser(entity, userId));
    }

    public PublicPagePublicationResponse publish( PublishPageRequest request, UUID actorId) {
        PublicPagePublication entity = new PublicPagePublication();
        if (request.id() != null) {
            entity.setId(request.id());
        }
        entity.setPage(refs.page(request.pageId()));
        entity.setRevision(refs.revision(request.revisionId()));
        entity.setPublicSlug(request.publicSlug());
        entity.setPublicTitle(request.publicTitle());
        entity.setPublicHtml(request.publicHtml());
        entity.setPublicationStatus(request.publicationStatus());
        entity.setPublishedBy(refs.user(actorId));
        entity.setPublishedAt(OffsetDateTime.now());
        return ApiMapper.toResponse(publicationService.publish(entity));
    }

    private Pageable sanitize( Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return pageable;
        }
        if (pageable.getPageSize() <= MAX_PAGE_SIZE) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
    }
}
