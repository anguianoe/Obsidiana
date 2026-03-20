package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.facade.WorkspaceFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
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
class WorkspaceControllerTest {

    @Mock
    private WorkspaceFacade workspaceFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private WorkspaceController controller;

    @Test
    void classRequiresUserRole() {
        PreAuthorize preAuthorize = WorkspaceController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('USER')");
    }

    @Test
    void accessibleUsesUserIdFromSession() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.accessible();

        verify(workspaceFacade).accessible(eq(userId));
    }
}
