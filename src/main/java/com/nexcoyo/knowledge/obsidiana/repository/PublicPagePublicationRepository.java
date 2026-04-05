package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.projection.PublicPageSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PublicPagePublicationRepository extends JpaRepository<PublicPagePublication, UUID> {
    Optional< PublicPagePublication > findByPublicSlugAndPublicationStatus( String publicSlug, PublicationStatus publicationStatus);
    Optional<PublicPagePublication> findByPageIdAndPublicationStatus(UUID pageId, PublicationStatus publicationStatus);
    List<PublicPagePublication> findAllByPageIdOrderByPublishedAtDesc(UUID pageId);

    @Query("""
        select p.id as publicationId,
               p.page.id as pageId,
               p.revision.id as revisionId,
               p.publicSlug as publicSlug,
               p.publicTitle as publicTitle,
               p.publishedAt as publishedAt
        from PublicPagePublication p
        where p.publicationStatus = com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus.LIVE
        order by p.publishedAt desc
    """)
    Page< PublicPageSummaryProjection > findLivePublicationSummaries( Pageable pageable);
}
