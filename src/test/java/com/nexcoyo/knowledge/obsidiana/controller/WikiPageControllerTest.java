package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.LinkPageToWorkspaceRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.facade.WikiPageFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.EditMode;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikiPageControllerTest {

    @Mock
    private WikiPageFacade wikiPageFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private WikiPageController controller;

    @Test
    void classRequiresUserRole() {
        PreAuthorize preAuthorize = WikiPageController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('USER')");
    }

    @Test
    void searchUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.search("text", UUID.randomUUID(), true, PageStatus.ACTIVE, Pageable.unpaged());

        verify(wikiPageFacade).search(eq("text"), any(UUID.class), eq(true), eq(PageStatus.ACTIVE), eq(Pageable.unpaged()), eq(false), eq(userId));
    }

    @Test
    void searchAccessibleUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.searchAccessible(workspaceId, tagId, "find", Pageable.unpaged());

        verify(wikiPageFacade).searchAccessible(userId, workspaceId, tagId, "find", Pageable.unpaged());
    }

    @Test
    void getByIdUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.getById(pageId);

        verify(wikiPageFacade).getById(pageId, userId);
    }

    @Test
    void createUsesSessionUserIdAndNonAdminFlag() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        WikiPageUpsertRequest request = new WikiPageUpsertRequest(
            null,
            null,
            "Title",
            "title",
            EditMode.SHARED,
            PageStatus.ACTIVE,
            false,
            true,
            null
        );

        controller.create(request);

        verify(wikiPageFacade).save(request, userId, false);
    }

    @Test
    void updateUsesSessionUserIdAndNonAdminFlag() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        WikiPageUpsertRequest request = new WikiPageUpsertRequest(
            UUID.randomUUID(),
            null,
            "Updated",
            "updated",
            EditMode.OWNER_ONLY,
            PageStatus.ARCHIVED,
            true,
            false,
            UUID.randomUUID()
        );

        controller.update(pageId, request);

        verify(wikiPageFacade).save(any(WikiPageUpsertRequest.class), eq(userId), eq(false));
    }

    @Test
    void linkToWorkspaceUsesSessionUserIdAndNonAdminFlag() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        LinkPageToWorkspaceRequest request = new LinkPageToWorkspaceRequest(UUID.randomUUID(), UUID.randomUUID());

        controller.linkToWorkspace(request);

        verify(wikiPageFacade).linkToWorkspace(request, userId, false);
    }

    @Test
    void treeUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID parentPageId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.tree(workspaceId, parentPageId);

        verify(wikiPageFacade).tree(workspaceId, parentPageId, userId);
    }
}


