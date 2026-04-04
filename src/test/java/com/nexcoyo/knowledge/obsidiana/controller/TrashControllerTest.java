package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.UserRestoreTrashRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserTrashRecordCreateRequest;
import com.nexcoyo.knowledge.obsidiana.facade.TrashFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrashControllerTest {

    @Mock
    private TrashFacade trashFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private TrashController controller;

    @Test
    void classRequiresUserRole() {
        PreAuthorize preAuthorize = TrashController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('USER')");
    }

    @Test
    void searchUsesSessionUserIdWithoutDeletedByFilter() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.search(TrashEntityType.PAGE, workspaceId, TrashStatus.TRASHED, Pageable.unpaged());

        verify(trashFacade).search(TrashEntityType.PAGE, workspaceId, TrashStatus.TRASHED, null, Pageable.unpaged(), userId);
    }

    @Test
    void getByIdUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        UUID trashRecordId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.getById(trashRecordId);

        verify(trashFacade).getById(trashRecordId, userId);
    }

    @Test
    void moveToTrashUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        UserTrashRecordCreateRequest request = new UserTrashRecordCreateRequest(
            null,
            TrashEntityType.PAGE,
            UUID.randomUUID(),
            null,
            UUID.randomUUID(),
            null,
            null,
            "cleanup",
            null,
            null,
            null,
            TrashStatus.TRASHED
        );

        controller.moveToTrash(request);

        verify(trashFacade).moveToTrashForUser(request, userId);
    }

    @Test
    void restoreUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        UUID trashRecordId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        UserRestoreTrashRequest request = new UserRestoreTrashRequest("restore");

        controller.restore(trashRecordId, request);

        verify(trashFacade).restoreForUser(trashRecordId, request, userId);
    }

    @Test
    void userEndpointsUseUserSpecificRequestTypes() throws Exception {
        Method moveToTrash = TrashController.class.getDeclaredMethod("moveToTrash", UserTrashRecordCreateRequest.class);
        Method restore = TrashController.class.getDeclaredMethod("restore", UUID.class, UserRestoreTrashRequest.class);

        assertThat(moveToTrash).isNotNull();
        assertThat(restore).isNotNull();
    }

    @Test
    void overdueUsesSessionUserId() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.overdue();

        verify(trashFacade).overdue(userId);
    }
}


