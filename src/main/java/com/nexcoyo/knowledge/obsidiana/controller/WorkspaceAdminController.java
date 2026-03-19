package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.InviteMemberRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateApprovalStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateMemberRoleRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.facade.WorkspaceFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/workspaces")
@RequiredArgsConstructor
public class WorkspaceAdminController
{
    private final WorkspaceFacade workspaceFacade;
    private final GeneralService generalService;

    @PostMapping
    public WorkspaceResponse create(@Valid @RequestBody WorkspaceUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.save(request, userId, false);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse adminGetById( @PathVariable UUID workspaceId) {
        return workspaceFacade.adminGetById(workspaceId);
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse adminUpdate(@PathVariable UUID workspaceId, @Valid @RequestBody WorkspaceUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.save(new WorkspaceUpsertRequest(
                workspaceId, request.name(), request.slug(), request.kind(), request.description()
        ), userId, true);
    }

    @GetMapping("/{workspaceId}/members")
    public List< WorkspaceMembershipResponse > adminActiveMembers( @PathVariable UUID workspaceId) {
        return workspaceFacade.activeMembers(workspaceId);
    }

    @DeleteMapping("/{workspaceId}")
    public void adminDelete(@PathVariable UUID workspaceId) {
        workspaceFacade.delete(workspaceId);
    }

    @GetMapping("/all")
    public PageResponse< WorkspaceResponse > listAll(
            @RequestParam(required = false) WorkspaceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return workspaceFacade.listAll(status, pageable);
    }

    @GetMapping("/search")
    public PageResponse< WorkspaceResponse > search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) WorkspaceKind kind,
            @RequestParam(required = false) WorkspaceStatus status,
            @RequestParam(required = false) UUID createdBy,
            Pageable pageable
    ) {
        return workspaceFacade.search(text, kind, status, createdBy, pageable);
    }

    @PatchMapping("/{workspaceId}/status/inactive")
    public WorkspaceResponse adminSetInactive(@PathVariable UUID workspaceId) {
        return workspaceFacade.setInactive(workspaceId);
    }

    @PatchMapping("/{workspaceId}/approval-status")
    public WorkspaceResponse updateApprovalStatus(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateApprovalStatusRequest request
    ) {
        UpdateApprovalStatusRequest data = new UpdateApprovalStatusRequest(request.approvalStatus(), generalService.getIdUserFromSession());
        return workspaceFacade.updateApprovalStatus(workspaceId, data);
    }

    @PostMapping("/{workspaceId}/members")
    public WorkspaceInvitationResponse adminInviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.inviteMember(workspaceId, request.userId(), request.role(), userId, true);
    }

    @PutMapping("/{workspaceId}/members/{memberId}")
    public WorkspaceMembershipResponse adminUpdateMemberRole(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.updateMemberRole(workspaceId, memberId, request.role(), userId, true);
    }

    @DeleteMapping("/{workspaceId}/members/{memberId}")
    public void adminRemoveMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId
    ) {
        UUID userId = generalService.getIdUserFromSession();
        workspaceFacade.removeMember(workspaceId, memberId, userId, true);
    }

    @PatchMapping("/{workspaceId}/restore")
    public WorkspaceResponse adminRestore(
            @PathVariable UUID workspaceId
    ) {
        return workspaceFacade.restoreWorkspace(workspaceId);
    }

    @GetMapping("/pending-approvals")
    public List< WorkspaceResponse > pendingApprovals() {
        return workspaceFacade.pendingGroupApprovals();
    }
}
