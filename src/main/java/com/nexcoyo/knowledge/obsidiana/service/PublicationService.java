package com.nexcoyo.knowledge.obsidiana.service;

import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.projection.PublicPageSummaryProjection;
import java.util.List;
import java.util.UUID;

public interface PublicationService {
    PublicPagePublication getLiveBySlug( String publicSlug);
    PublicPagePublication getLiveByPageId(UUID pageId);
    PublicPagePublication publish(PublicPagePublication publication);
    List< PublicPageSummaryProjection > getLiveSummaries();
}
