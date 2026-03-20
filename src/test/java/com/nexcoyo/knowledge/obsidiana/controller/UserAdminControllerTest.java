package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignUserTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserWorkspaceMembershipRequest;
import com.nexcoyo.knowledge.obsidiana.facade.UserAdminFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminControllerTest {

    @Mock
    private UserAdminFacade userAdminFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private UserAdminController controller;

    @Test
    void classRequiresSuperAdminRole() {
        PreAuthorize preAuthorize = UserAdminController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void assignTagUsesActorFromSession() {
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        AssignUserTagRequest request = new AssignUserTagRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "ACTIVE");

        when(generalService.getIdUserFromSession()).thenReturn(actorId);

        controller.assignTag(userId, request);

        verify(userAdminFacade).assignTag(eq(userId), eq(request), eq(actorId));
    }

    @Test
    void addToWorkspaceSupportsRestAndLegacyRoutes() throws Exception {
        Method method = UserAdminController.class.getDeclaredMethod("addToWorkspace", UUID.class, UpsertUserWorkspaceMembershipRequest.class);
        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertThat(mapping).isNotNull();
        assertThat(Arrays.asList(mapping.value()))
                .contains("/{userId}/workspaces", "/{userId}/add-to-workspaces");
    }
}
