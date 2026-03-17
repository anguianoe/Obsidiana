package com.nexcoyo.knowledge.obsidiana.service;

import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.projection.RevisionSummaryProjection;

import java.util.List;
import java.util.UUID;

public interface WikiPageRevisionService {
    WikiPageRevision getRequired(UUID revisionId);
    WikiPageRevision getLatestRevision( UUID pageId);
    List< RevisionSummaryProjection > getRevisionSummary( UUID pageId);
    WikiPageRevision saveRevision(WikiPageRevision revision, boolean updatePagePointer);
}
