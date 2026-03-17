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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashFacade trashFacade;

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

    @GetMapping("/{trashRecordId}")
    public TrashRecordResponse getById(@PathVariable UUID trashRecordId) {
        return trashFacade.getById(trashRecordId);
    }

    @PostMapping
    public TrashRecordResponse moveToTrash(@Valid @RequestBody TrashRecordCreateRequest request) {
        return trashFacade.moveToTrash(request);
    }

    @PostMapping("/{trashRecordId}/restore")
    public RestoreAuditResponse restore( @PathVariable UUID trashRecordId, @Valid @RequestBody RestoreTrashRequest request) {
        return trashFacade.restore(trashRecordId, request);
    }

    @GetMapping("/overdue")
    public List<TrashRecordResponse> overdue() {
        return trashFacade.overdue();
    }
}
