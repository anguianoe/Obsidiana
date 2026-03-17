package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.facade.WorkspaceFacade;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceFacade workspaceFacade;

    @GetMapping
    public PageResponse< WorkspaceResponse > search(
        @RequestParam(required = false) String text,
        @RequestParam(required = false) WorkspaceKind kind,
        @RequestParam(required = false) WorkspaceStatus status,
        @RequestParam(required = false) UUID createdBy,
        Pageable pageable
    ) {
        return workspaceFacade.search(text, kind, status, createdBy, pageable);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse getById(@PathVariable UUID workspaceId) {
        return workspaceFacade.getById(workspaceId);
    }

    @PostMapping
    public WorkspaceResponse create(@Valid @RequestBody WorkspaceUpsertRequest request) {
        return workspaceFacade.save(request);
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse update(@PathVariable UUID workspaceId, @Valid @RequestBody WorkspaceUpsertRequest request) {
        return workspaceFacade.save(new WorkspaceUpsertRequest(
            workspaceId, request.name(), request.slug(), request.kind(), request.status(),
            request.approvalStatus(), request.createdBy(), request.approvedBy(), request.description()
        ));
    }

    @GetMapping("/accessible/{userId}")
    public List< WorkspaceSummaryResponse > accessible( @PathVariable UUID userId) {
        return workspaceFacade.accessible(userId);
    }

    @GetMapping("/{workspaceId}/members")
    public List< WorkspaceMembershipResponse > activeMembers( @PathVariable UUID workspaceId) {
        return workspaceFacade.activeMembers(workspaceId);
    }

    @GetMapping("/{workspaceId}/invitations/pending")
    public List< WorkspaceInvitationResponse > pendingInvitations( @PathVariable UUID workspaceId) {
        return workspaceFacade.pendingInvitations(workspaceId);
    }
}
