package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.projection.RevisionSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRevisionRepository;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageRevisionService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WikiPageRevisionServiceImpl implements WikiPageRevisionService {

    private final WikiPageRevisionRepository wikiPageRevisionRepository;
    private final WikiPageRepository wikiPageRepository;

    @Override
    public WikiPageRevision getRequired( UUID revisionId) {
        return wikiPageRevisionRepository.findById(revisionId)
            .orElseThrow(() -> new EntityNotFoundException("Revision not found: " + revisionId));
    }

    @Override
    public WikiPageRevision getLatestRevision(UUID pageId) {
        return wikiPageRevisionRepository.findTopByPageIdOrderByRevisionNumberDesc(pageId)
            .orElseThrow(() -> new EntityNotFoundException("No revisions found for page: " + pageId));
    }

    @Override
    public List< RevisionSummaryProjection > getRevisionSummary( UUID pageId) {
        return wikiPageRevisionRepository.findRevisionSummaryByPageId(pageId);
    }

    @Override
    @Transactional
    public WikiPageRevision saveRevision(WikiPageRevision revision, boolean updatePagePointer) {
        WikiPageRevision saved = wikiPageRevisionRepository.save(revision);
        if (updatePagePointer) {
            WikiPage page = revision.getPage();
            page.setCurrentRevision(saved);
            wikiPageRepository.save(page);
        }
        return saved;
    }
}
