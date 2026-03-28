package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageRevisionCreateRequest;
import com.nexcoyo.knowledge.obsidiana.facade.WikiPageRevisionFacade;
import com.nexcoyo.knowledge.obsidiana.facade.support.AccessContext;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikiPageRevisionControllerTest {

    @Mock
    private WikiPageRevisionFacade revisionFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private WikiPageRevisionController controller;

    @Test
    void classRequiresUserRole() {
        PreAuthorize preAuthorize = WikiPageRevisionController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('USER')");
    }

    @Test
    void getByIdUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.getById(revisionId);

        verify(revisionFacade).getById(eq(revisionId), argThat(ctx -> ctx instanceof AccessContext.User && userId.equals(ctx.actorUserId())));
    }

    @Test
    void latestUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.latest(pageId);

        verify(revisionFacade).latest(eq(pageId), argThat(ctx -> ctx instanceof AccessContext.User && userId.equals(ctx.actorUserId())));
    }

    @Test
    void summaryUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.summary(pageId, pageable);

        verify(revisionFacade).summary(eq(pageId), argThat(ctx -> ctx instanceof AccessContext.User && userId.equals(ctx.actorUserId())), eq(pageable));
    }

    @Test
    void createPassesSessionUserAsAccessContext() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        WikiPageRevisionCreateRequest request = new WikiPageRevisionCreateRequest(
            null,
            UUID.randomUUID(),
            7,
            "Title",
            EditorType.CKEDITOR,
            "<p>html</p>",
            "text",
            null,
            "summary",
            false,
            null,
            null,
            null,
            false,
            null,
            true
        );

        controller.create(request);

        verify(revisionFacade).save(any(WikiPageRevisionCreateRequest.class), argThat(ctx -> ctx instanceof AccessContext.User && userId.equals(ctx.actorUserId())));
    }

    @Test
    void newVersionPassesSessionUserAsAccessContext() {
        UUID userId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        WikiPageRevisionCreateRequest request = new WikiPageRevisionCreateRequest(
            null,
            UUID.randomUUID(),
            8,
            "Title 2",
            EditorType.CKEDITOR,
            "<p>html2</p>",
            "text2",
            null,
            "summary2",
            false,
            null,
            null,
            null,
            true,
            null,
            true
        );

        controller.newVersion(revisionId, request);

        verify(revisionFacade).save(any(WikiPageRevisionCreateRequest.class), argThat(ctx -> ctx instanceof AccessContext.User && userId.equals(ctx.actorUserId())));
    }

    @Test
    void restoreUsesSessionUserAsAccessContext() {
        UUID userId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.restore(revisionId);

        verify(revisionFacade).restore(eq(revisionId), argThat(ctx -> ctx instanceof AccessContext.User && userId.equals(ctx.actorUserId())));
    }
}

