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

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikiPageAdminControllerTest {

    @Mock
    private WikiPageFacade wikiPageFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private WikiPageAdminController controller;

    @Test
    void classRequiresSuperAdminRole() {
        PreAuthorize preAuthorize = WikiPageAdminController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void hasNoCreateEndpoint() {
        boolean hasCreateMethod = Arrays.stream(WikiPageAdminController.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().equals("create"));

        assertThat(hasCreateMethod).isFalse();
    }

    @Test
    void searchUsesAdminFlagAndNullUserContract() {
        controller.search("text", UUID.randomUUID(), false, PageStatus.ACTIVE, Pageable.unpaged());

        verify(wikiPageFacade).search(eq("text"), any(UUID.class), eq(false), eq(PageStatus.ACTIVE), eq(Pageable.unpaged()), eq(true), eq(null));
    }

    @Test
    void getByIdUsesNullUserIdForAdminContract() {
        UUID pageId = UUID.randomUUID();

        controller.getById(pageId);

        verify(wikiPageFacade).getById(pageId, null);
    }

    @Test
    void updateUsesActorFromSessionAndAdminFlagTrue() {
        UUID actorId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        WikiPageUpsertRequest request = new WikiPageUpsertRequest(
            null,
            null,
            "Admin Update",
            "admin-update",
            EditMode.SHARED,
            PageStatus.ACTIVE,
            false,
            true,
            null
        );

        controller.update(pageId, request);

        verify(wikiPageFacade).save(any(WikiPageUpsertRequest.class), eq(actorId), eq(true));
    }

    @Test
    void linkToWorkspaceUsesActorFromSessionAndAdminFlagTrue() {
        UUID actorId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);
        LinkPageToWorkspaceRequest request = new LinkPageToWorkspaceRequest(UUID.randomUUID(), UUID.randomUUID());

        controller.linkToWorkspace(request);

        verify(wikiPageFacade).linkToWorkspace(request, actorId, true);
    }

    @Test
    void treeUsesNullUserIdForAdminContract() {
        UUID workspaceId = UUID.randomUUID();

        controller.tree(workspaceId, null);

        verify(wikiPageFacade).tree(workspaceId, null, null);
    }
}


