package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.dto.request.PublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPagePublicationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPageSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicationFacade {

    private final PublicationService publicationService;
    private final EntityReferenceResolver refs;

    public PublicPagePublicationResponse getLiveBySlug( String slug) {
        return ApiMapper.toResponse(publicationService.getLiveBySlug(slug));
    }

    public PublicPagePublicationResponse getLiveByPageId(UUID pageId) {
        return ApiMapper.toResponse(publicationService.getLiveByPageId(pageId));
    }

    public List< PublicPageSummaryResponse > liveSummaries() {
        return publicationService.getLiveSummaries().stream().map(ApiMapper::toResponse).toList();
    }

    public PublicPagePublicationResponse publish( PublishPageRequest request) {
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
        entity.setPublishedBy(refs.user(request.publishedBy()));
        entity.setPublishedAt(OffsetDateTime.now());
        return ApiMapper.toResponse(publicationService.publish(entity));
    }
}
