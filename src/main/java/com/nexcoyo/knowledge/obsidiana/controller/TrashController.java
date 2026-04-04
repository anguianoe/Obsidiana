package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserRestoreTrashRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserTrashRecordCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.RestoreAuditResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.TrashRecordResponse;
import com.nexcoyo.knowledge.obsidiana.facade.TrashFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
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
@RequestMapping("/api/v1/trash")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TrashController {

    private final TrashFacade trashFacade;
    private final GeneralService generalService;

    /**
     * USER scope:
     * - returns records deleted by the current user, or visible through active workspace/page/comment/asset relationships.
     */
    @GetMapping
    public PageResponse< TrashRecordResponse > search(
        @RequestParam(required = false) TrashEntityType entityType,
        @RequestParam(required = false) UUID workspaceId,
        @RequestParam(required = false) TrashStatus status,
        Pageable pageable
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return trashFacade.search(entityType, workspaceId, status, null, pageable, userId);
    }

    /**
     * USER scope:
     * - returns only records whose {@code deletedBy} is the current session user.
     */
    @GetMapping("/{trashRecordId}")
    public TrashRecordResponse getById(@PathVariable UUID trashRecordId) {
        UUID userId = generalService.getIdUserFromSession();
        return trashFacade.getById(trashRecordId, userId);
    }

    /**
     * USER scope:
     * - can move to trash only resources created/owned by the current user, or accessible through active workspace/page/comment relationships.
     * - {@code deletedBy} is always derived from session.
     */
    @PostMapping
    public TrashRecordResponse moveToTrash(@Valid @RequestBody UserTrashRecordCreateRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return trashFacade.moveToTrashForUser(request, userId);
    }

    /**
     * USER scope:
     * - restore is allowed for records deleted by the current user, or accessible through related workspace/page/comment/asset ownership rules.
     * - audit actor is always derived from session.
     */
    @PostMapping("/{trashRecordId}/restore")
    public RestoreAuditResponse restore( @PathVariable UUID trashRecordId, @Valid @RequestBody UserRestoreTrashRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return trashFacade.restoreForUser(trashRecordId, request, userId);
    }

    /**
     * USER scope:
     * - returns overdue trash records only when {@code deletedBy} is the current session user.
     */
    @GetMapping("/overdue")
    public List<TrashRecordResponse> overdue() {
        UUID userId = generalService.getIdUserFromSession();
        return trashFacade.overdue(userId);
    }
}
