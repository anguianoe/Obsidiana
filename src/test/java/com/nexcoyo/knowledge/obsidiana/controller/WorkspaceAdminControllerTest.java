package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.facade.WorkspaceFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceAdminControllerTest {

    @Mock
    private WorkspaceFacade workspaceFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private WorkspaceAdminController controller;

    @Test
    void classRequiresSuperAdminRole() {
        PreAuthorize preAuthorize = WorkspaceAdminController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void createUsesAdminFlagTrue() {
        UUID actorId = UUID.randomUUID();
        WorkspaceUpsertRequest request = new WorkspaceUpsertRequest(null, "Admin Workspace", "admin-workspace", WorkspaceKind.GROUP, "desc");

        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        controller.create(request);

        verify(workspaceFacade).save(eq(request), eq(actorId), eq(true));
    }
}
