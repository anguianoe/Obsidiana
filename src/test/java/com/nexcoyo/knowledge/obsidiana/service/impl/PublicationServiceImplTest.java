package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.common.exception.ApiException;
import com.nexcoyo.knowledge.obsidiana.common.exception.ErrorCode;
import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.repository.PublicPagePublicationRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRevisionRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationServiceImplTest {

    @Mock
    private PublicPagePublicationRepository publicationRepository;
    @Mock
    private WikiPageRepository wikiPageRepository;
    @Mock
    private WikiPageRevisionRepository wikiPageRevisionRepository;

    @InjectMocks
    private PublicationServiceImpl service;

    @Test
    void publishForUserAllowsAccessiblePage() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        PublicPagePublication publication = publication(pageId);
        publication.getRevision().setId(revisionId);
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(true);
        when(wikiPageRevisionRepository.existsByIdAndPageId(revisionId, pageId)).thenReturn(true);

        service.publishForUser(publication, userId);

        verify(publicationRepository).save(publication);
    }

    @Test
    void publishForUserRejectsInaccessiblePage() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        PublicPagePublication publication = publication(pageId);
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.publishForUser(publication, userId))
            .isInstanceOfSatisfying(ApiException.class, ex -> {
                org.assertj.core.api.Assertions.assertThat(ex.code()).isEqualTo(ErrorCode.FORBIDDEN);
                org.assertj.core.api.Assertions.assertThat(ex.getMessage()).contains("do not have access");
            });
    }

    @Test
    void publishForUserRejectsRevisionPageMismatch() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        PublicPagePublication publication = publication(pageId);
        publication.getRevision().setId(revisionId);
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(true);
        when(wikiPageRevisionRepository.existsByIdAndPageId(revisionId, pageId)).thenReturn(false);

        assertThatThrownBy(() -> service.publishForUser(publication, userId))
            .isInstanceOfSatisfying(ApiException.class, ex -> {
                org.assertj.core.api.Assertions.assertThat(ex.code()).isEqualTo(ErrorCode.REVISION_PAGE_MISMATCH);
            });
    }

    @Test
    void publishAdminRejectsRevisionPageMismatch() {
        UUID pageId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        PublicPagePublication publication = publication(pageId);
        publication.getRevision().setId(revisionId);
        when(wikiPageRevisionRepository.existsByIdAndPageId(revisionId, pageId)).thenReturn(false);

        assertThatThrownBy(() -> service.publish(publication))
            .isInstanceOfSatisfying(ApiException.class, ex -> {
                org.assertj.core.api.Assertions.assertThat(ex.code()).isEqualTo(ErrorCode.REVISION_PAGE_MISMATCH);
            });
    }

    @Test
    void liveSummariesUsesPageableRepositoryQuery() {
        Pageable pageable = Pageable.unpaged();

        service.getLiveSummaries(pageable);

        verify(publicationRepository).findLivePublicationSummaries(pageable);
    }

    private PublicPagePublication publication(UUID pageId) {
        WikiPage page = new WikiPage();
        page.setId(pageId);
        WikiPageRevision revision = new WikiPageRevision();
        PublicPagePublication publication = new PublicPagePublication();
        publication.setPage(page);
        publication.setRevision(revision);
        return publication;
    }
}

