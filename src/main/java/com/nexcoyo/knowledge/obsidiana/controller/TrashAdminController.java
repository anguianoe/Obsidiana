package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.RestoreTrashRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.TrashRecordCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.RestoreAuditResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.TrashRecordResponse;
import com.nexcoyo.knowledge.obsidiana.facade.TrashFacade;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/trash")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TrashAdminController {

    private final TrashFacade trashFacade;

    /**
     * ADMIN scope:
     * - unrestricted search across trash records.
     */
    @GetMapping
    public PageResponse< TrashRecordResponse > search(
            @RequestParam(required = false) TrashEntityType entityType,
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) TrashStatus status,
            @RequestParam(required = false) UUID deletedBy,
            Pageable pageable
    ) {
        return trashFacade.search(entityType, workspaceId, status, deletedBy, pageable);
    }

    /**
     * ADMIN scope:
     * - unrestricted get by id.
     */
    @GetMapping("/{trashRecordId}")
    public TrashRecordResponse getById(@PathVariable UUID trashRecordId) {
        return trashFacade.getById(trashRecordId);
    }

    /**
     * ADMIN scope:
     * - unrestricted move-to-trash using explicit actor fields from request.
     */
    @PostMapping
    public TrashRecordResponse moveToTrash(@Valid @RequestBody TrashRecordCreateRequest request) {
        return trashFacade.moveToTrash(request);
    }

    /**
     * ADMIN scope:
     * - unrestricted restore using explicit actor fields from request.
     */
    @PostMapping("/{trashRecordId}/restore")
    public RestoreAuditResponse restore( @PathVariable UUID trashRecordId, @Valid @RequestBody RestoreTrashRequest request) {
        return trashFacade.restore(trashRecordId, request);
    }

    /**
     * ADMIN scope:
     * - unrestricted overdue listing.
     */
    @GetMapping("/overdue")
    public List<TrashRecordResponse> overdue() {
        return trashFacade.overdue();
    }
}
