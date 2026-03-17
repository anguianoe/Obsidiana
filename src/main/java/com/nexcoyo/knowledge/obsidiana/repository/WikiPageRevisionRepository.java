package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.projection.RevisionSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WikiPageRevisionRepository extends JpaRepository< WikiPageRevision, UUID> {

    Optional<WikiPageRevision> findTopByPageIdOrderByRevisionNumberDesc(UUID pageId);

    List<WikiPageRevision> findAllByPageIdOrderByRevisionNumberDesc(UUID pageId);

    @Query("""
        select r.id as revisionId,
               r.revisionNumber as revisionNumber,
               r.titleSnapshot as titleSnapshot,
               r.isPinned as pinned,
               r.createdBy.id as createdBy,
               r.createdAt as createdAt
        from WikiPageRevision r
        where r.page.id = :pageId
        order by r.revisionNumber desc
    """)
    List< RevisionSummaryProjection > findRevisionSummaryByPageId( @Param("pageId") UUID pageId);

    @Query("""
        select r
        from WikiPageRevision r
        where r.page.id = :pageId
          and (r.isPinned = true or r.id in (
                select p.revision.id from PublicPagePublication p where p.page.id = :pageId
          ))
        order by r.revisionNumber desc
    """)
    List<WikiPageRevision> findProtectedRevisions(@Param("pageId") UUID pageId);
}
