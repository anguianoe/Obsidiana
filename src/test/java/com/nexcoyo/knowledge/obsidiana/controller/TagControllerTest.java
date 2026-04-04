package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceTagUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.facade.TagFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.TagStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagFacade tagFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private TagController controller;

    @Test
    void classRequiresUserRole() {
        PreAuthorize preAuthorize = TagController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('USER')");
    }

    @Test
    void activeTagsUsesSessionUser() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.activeTags(workspaceId);

        verify(tagFacade).activeTagsForUser(workspaceId, userId);
    }

    @Test
    void createUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        WorkspaceTagUpsertRequest request = new WorkspaceTagUpsertRequest(null, UUID.randomUUID(), "tag", TagStatus.ACTIVE, UUID.randomUUID());

        controller.create(request);

        verify(tagFacade).saveForUser(request, userId);
    }

    @Test
    void updateUsesSessionUserAsActor() {
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        WorkspaceTagUpsertRequest request = new WorkspaceTagUpsertRequest(null, workspaceId, "tag", TagStatus.INACTIVE, UUID.randomUUID());

        controller.update(tagId, request);

        verify(tagFacade).saveForUser(new WorkspaceTagUpsertRequest(tagId, workspaceId, "tag", TagStatus.INACTIVE, userId), userId);
    }

    @Test
    void assignUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        AssignTagRequest request = new AssignTagRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        controller.assign(request);

        verify(tagFacade).assignForUser(request, userId);
    }

    @Test
    void assignmentsUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.assignments(pageId, workspaceId);

        verify(tagFacade).assignmentsForUser(pageId, workspaceId, userId);
    }
}

