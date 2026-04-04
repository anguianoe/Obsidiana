package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.RestoreTrashRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.TrashRecordCreateRequest;
import com.nexcoyo.knowledge.obsidiana.facade.TrashFacade;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
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

@ExtendWith(MockitoExtension.class)
class TrashAdminControllerTest {

    @Mock
    private TrashFacade trashFacade;

    @InjectMocks
    private TrashAdminController controller;

    @Test
    void classRequiresSuperAdminRole() {
        PreAuthorize preAuthorize = TrashAdminController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void searchUsesExplicitDeletedByFilter() {
        UUID workspaceId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        controller.search(TrashEntityType.WORKSPACE, workspaceId, TrashStatus.TRASHED, deletedBy, Pageable.unpaged());

        verify(trashFacade).search(TrashEntityType.WORKSPACE, workspaceId, TrashStatus.TRASHED, deletedBy, Pageable.unpaged());
    }

    @Test
    void getByIdUsesAdminFacadeMethod() {
        UUID trashRecordId = UUID.randomUUID();

        controller.getById(trashRecordId);

        verify(trashFacade).getById(trashRecordId);
    }

    @Test
    void moveToTrashUsesAdminFacadeMethod() {
        TrashRecordCreateRequest request = new TrashRecordCreateRequest(
            null,
            TrashEntityType.ASSET,
            UUID.randomUUID(),
            null,
            null,
            UUID.randomUUID(),
            null,
            UUID.randomUUID(),
            "cleanup",
            null,
            null,
            null,
            TrashStatus.TRASHED
        );

        controller.moveToTrash(request);

        verify(trashFacade).moveToTrash(request);
    }

    @Test
    void restoreUsesAdminFacadeMethod() {
        UUID trashRecordId = UUID.randomUUID();
        RestoreTrashRequest request = new RestoreTrashRequest(UUID.randomUUID(), "restore");

        controller.restore(trashRecordId, request);

        verify(trashFacade).restore(trashRecordId, request);
    }

    @Test
    void overdueUsesAdminFacadeMethod() {
        controller.overdue();

        verify(trashFacade).overdue();
    }
}

