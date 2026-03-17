package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.AuditEventCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.AuditEventResponse;
import com.nexcoyo.knowledge.obsidiana.facade.AuditFacade;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditFacade auditFacade;

    @PostMapping
    public AuditEventResponse create(@Valid @RequestBody AuditEventCreateRequest request) {
        return auditFacade.save(request);
    }

    @GetMapping("/workspace/{workspaceId}")
    public PageResponse<AuditEventResponse> workspaceEvents( @PathVariable UUID workspaceId, Pageable pageable) {
        return auditFacade.workspaceEvents(workspaceId, pageable);
    }

    @GetMapping("/timeline")
    public List< AuditEventResponse > timeline( @RequestParam String entityType, @RequestParam UUID entityId) {
        return auditFacade.timeline(entityType, entityId);
    }
}
