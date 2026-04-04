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
class TagAdminControllerTest {

    @Mock
    private TagFacade tagFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private TagAdminController controller;

    @Test
    void classRequiresSuperAdminRole() {
        PreAuthorize preAuthorize = TagAdminController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void activeTagsUsesAdminFacadeMethod() {
        UUID workspaceId = UUID.randomUUID();

        controller.activeTags(workspaceId);

        verify(tagFacade).activeTags(workspaceId);
    }

    @Test
    void createUsesAdminFacadeMethod() {
        UUID actorId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);
        WorkspaceTagUpsertRequest request = new WorkspaceTagUpsertRequest(null, UUID.randomUUID(), "tag", TagStatus.ACTIVE, UUID.randomUUID());

        controller.create(request);

        verify(tagFacade).save(new WorkspaceTagUpsertRequest(request.id(), request.workspaceId(), request.name(), request.tagStatus(), actorId));
    }

    @Test
    void updateUsesAdminFacadeMethod() {
        UUID actorId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);
        WorkspaceTagUpsertRequest request = new WorkspaceTagUpsertRequest(null, UUID.randomUUID(), "tag", TagStatus.INACTIVE, UUID.randomUUID());

        controller.update(tagId, request);

        verify(tagFacade).save(new WorkspaceTagUpsertRequest(tagId, request.workspaceId(), request.name(), request.tagStatus(), actorId));
    }

    @Test
    void assignUsesAdminFacadeMethod() {
        UUID actorId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);
        AssignTagRequest request = new AssignTagRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        controller.assign(request);

        verify(tagFacade).assign(new AssignTagRequest(request.pageId(), request.workspaceId(), request.tagId(), actorId));
    }

    @Test
    void assignmentsUsesAdminFacadeMethod() {
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        controller.assignments(pageId, workspaceId);

        verify(tagFacade).assignments(pageId, workspaceId);
    }
}


