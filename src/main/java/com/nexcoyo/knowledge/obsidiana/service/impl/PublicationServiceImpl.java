package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.projection.PublicPageSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.repository.PublicPagePublicationRepository;
import com.nexcoyo.knowledge.obsidiana.service.PublicationService;
import com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicationServiceImpl implements PublicationService {

    private final PublicPagePublicationRepository publicPagePublicationRepository;

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
    public PublicPagePublication publish(PublicPagePublication publication) {
        return publicPagePublicationRepository.save(publication);
    }

    @Override
    public List< PublicPageSummaryProjection > getLiveSummaries() {
        return publicPagePublicationRepository.findLivePublicationSummaries();
    }
}
