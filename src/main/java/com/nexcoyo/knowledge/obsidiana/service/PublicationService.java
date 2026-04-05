package com.nexcoyo.knowledge.obsidiana.service;

import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.projection.PublicPageSummaryProjection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublicationService {
    PublicPagePublication getLiveBySlug( String publicSlug);
    PublicPagePublication getLiveByPageId(UUID pageId);
    PublicPagePublication publishForUser(PublicPagePublication publication, UUID userId);
    PublicPagePublication publish(PublicPagePublication publication);
    Page< PublicPageSummaryProjection > getLiveSummaries( Pageable pageable);
}
