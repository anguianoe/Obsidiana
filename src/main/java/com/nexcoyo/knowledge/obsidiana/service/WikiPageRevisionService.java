package com.nexcoyo.knowledge.obsidiana.service;

import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.projection.RevisionSummaryProjection;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WikiPageRevisionService {
    WikiPageRevision getRequired(UUID revisionId);
    WikiPageRevision getLatestRevision( UUID pageId);
    Page<RevisionSummaryProjection> getRevisionSummary(UUID pageId, Pageable pageable);
    WikiPageRevision saveRevision(WikiPageRevision revision, boolean updatePagePointer);
    WikiPageRevision restoreRevision(UUID revisionId, UUID actorUserId);
}
