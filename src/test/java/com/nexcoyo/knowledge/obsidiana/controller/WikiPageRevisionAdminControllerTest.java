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
class WikiPageRevisionAdminControllerTest {

    @Mock
    private WikiPageRevisionFacade revisionFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private WikiPageRevisionAdminController controller;

    @Test
    void classRequiresSuperAdminRole() {
        PreAuthorize preAuthorize = WikiPageRevisionAdminController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void getByIdUsesAdminAccessContext() {
        UUID actorId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        controller.getById(revisionId);

        verify(revisionFacade).getById(eq(revisionId), argThat(ctx -> ctx instanceof AccessContext.Admin && actorId.equals(ctx.actorUserId())));
    }

    @Test
    void latestUsesAdminAccessContext() {
        UUID actorId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        controller.latest(pageId);

        verify(revisionFacade).latest(eq(pageId), argThat(ctx -> ctx instanceof AccessContext.Admin && actorId.equals(ctx.actorUserId())));
    }

    @Test
    void summaryUsesAdminAccessContext() {
        UUID actorId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        controller.summary(pageId, pageable);

        verify(revisionFacade).summary(eq(pageId), argThat(ctx -> ctx instanceof AccessContext.Admin && actorId.equals(ctx.actorUserId())), eq(pageable));
    }

    @Test
    void createUsesAdminBypassAccessContext() {
        UUID actorId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        WikiPageRevisionCreateRequest request = new WikiPageRevisionCreateRequest(
            null,
            UUID.randomUUID(),
            1,
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

        verify(revisionFacade).save(any(WikiPageRevisionCreateRequest.class), argThat(ctx -> ctx instanceof AccessContext.Admin && actorId.equals(ctx.actorUserId())));
    }

    @Test
    void newVersionUsesAdminBypassAccessContext() {
        UUID actorId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        WikiPageRevisionCreateRequest request = new WikiPageRevisionCreateRequest(
            null,
            UUID.randomUUID(),
            2,
            "Title2",
            EditorType.CKEDITOR,
            "<p>html2</p>",
            "text2",
            null,
            "summary2",
            false,
            null,
            null,
            null,
            false,
            null,
            true
        );

        controller.newVersion(revisionId, request);

        verify(revisionFacade).save(any(WikiPageRevisionCreateRequest.class), argThat(ctx -> ctx instanceof AccessContext.Admin && actorId.equals(ctx.actorUserId())));
    }

    @Test
    void restoreUsesAdminAccessContext() {
        UUID actorId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        controller.restore(revisionId);

        verify(revisionFacade).restore(eq(revisionId), argThat(ctx -> ctx instanceof AccessContext.Admin && actorId.equals(ctx.actorUserId())));
    }
}


