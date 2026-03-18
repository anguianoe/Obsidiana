package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.InviteMemberRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.RespondInvitationRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateApprovalStatusRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateMemberRoleRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.facade.WorkspaceFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceFacade workspaceFacade;
    private final GeneralService generalService;

    @PostMapping
    public WorkspaceResponse create(@Valid @RequestBody WorkspaceUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.save(request, userId, false);
    }

    @GetMapping("/admin/{workspaceId}")
    public WorkspaceResponse adminGetById(@PathVariable UUID workspaceId) {
        return workspaceFacade.adminGetById(workspaceId);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse getById(@PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.getById(workspaceId, userId);
    }

    @PutMapping("/admin/{workspaceId}")
    public WorkspaceResponse adminUpdate(@PathVariable UUID workspaceId, @Valid @RequestBody WorkspaceUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.save(new WorkspaceUpsertRequest(
                workspaceId, request.name(), request.slug(), request.kind(), request.status(),
                request.approvalStatus(), request.createdBy(), request.approvedBy(), request.description()
        ), userId, false);
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse update(@PathVariable UUID workspaceId, @Valid @RequestBody WorkspaceUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.save(new WorkspaceUpsertRequest(
            workspaceId, request.name(), request.slug(), request.kind(), request.status(),
            request.approvalStatus(), request.createdBy(), request.approvedBy(), request.description()
        ), userId,true);
    }

    @GetMapping("/accessible/{userId}")
    public List< WorkspaceSummaryResponse > accessible( @PathVariable UUID userId) {
        return workspaceFacade.accessible(userId);
    }

    @GetMapping("/admin/{workspaceId}/members")
    public List< WorkspaceMembershipResponse > adminActiveMembers( @PathVariable UUID workspaceId) {
        return workspaceFacade.activeMembers(workspaceId);
    }

    @GetMapping("/{workspaceId}/members")
    public List< WorkspaceMembershipResponse > activeMembers( @PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.activeMembers(workspaceId, userId);
    }

    @DeleteMapping("/admin/{workspaceId}")
    public void adminDelete(@PathVariable UUID workspaceId) {
        workspaceFacade.delete(workspaceId);
    }

    @DeleteMapping("/{workspaceId}")
    public void delete(@PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        workspaceFacade.delete(workspaceId, userId);
    }


    @GetMapping("/admin/all")
    public PageResponse< WorkspaceResponse > listAll(
            @RequestParam(required = false) WorkspaceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return workspaceFacade.listAll(status, pageable);
    }

    @GetMapping("/admin/search")
    public PageResponse< WorkspaceResponse > search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) WorkspaceKind kind,
            @RequestParam(required = false) WorkspaceStatus status,
            @RequestParam(required = false) UUID createdBy,
            Pageable pageable
    ) {
        return workspaceFacade.search(text, kind, status, createdBy, pageable);
    }

    @GetMapping("/my")
    public PageResponse< WorkspaceResponse > searchMyWorkspaces(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) WorkspaceStatus status,
            Pageable pageable
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.searchByCreatedBy(userId, text, status, pageable);
    }

    @GetMapping("/{workspaceId}/invitations/pending")
    public List< WorkspaceInvitationResponse > pendingInvitations( @PathVariable UUID workspaceId) {
        return workspaceFacade.pendingInvitations(workspaceId);
    }

    @PatchMapping("/admin/{workspaceId}/status/inactive")
    public WorkspaceResponse adminSetInactive(@PathVariable UUID workspaceId) {
        return workspaceFacade.setInactive(workspaceId);
    }

    @PatchMapping("/{workspaceId}/status/inactive")
    public WorkspaceResponse setInactive(@PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.setInactive(workspaceId, userId);
    }

    @PatchMapping("/admin/{workspaceId}/approval-status")
    public WorkspaceResponse updateApprovalStatus(
        @PathVariable UUID workspaceId,
        @Valid @RequestBody UpdateApprovalStatusRequest request
    ) {
        return workspaceFacade.updateApprovalStatus(workspaceId, request);
    }

    // ========== GESTIÓN DE MIEMBROS ==========

    @PostMapping("/{workspaceId}/members")
    public WorkspaceInvitationResponse inviteMember(
        @PathVariable UUID workspaceId,
        @Valid @RequestBody InviteMemberRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.inviteMember(workspaceId, request.userId(), request.role(), userId, false);
    }

    @PostMapping("/admin/{workspaceId}/members")
    public WorkspaceInvitationResponse adminInviteMember(
        @PathVariable UUID workspaceId,
        @Valid @RequestBody InviteMemberRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.inviteMember(workspaceId, request.userId(), request.role(), userId, true);
    }

    @PutMapping("/{workspaceId}/members/{memberId}")
    public WorkspaceMembershipResponse updateMemberRole(
        @PathVariable UUID workspaceId,
        @PathVariable UUID memberId,
        @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.updateMemberRole(workspaceId, memberId, request.role(), userId, false);
    }

    @PutMapping("/admin/{workspaceId}/members/{memberId}")
    public WorkspaceMembershipResponse adminUpdateMemberRole(
        @PathVariable UUID workspaceId,
        @PathVariable UUID memberId,
        @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.updateMemberRole(workspaceId, memberId, request.role(), userId, true);
    }

    @DeleteMapping("/{workspaceId}/members/{memberId}")
    public void removeMember(
        @PathVariable UUID workspaceId,
        @PathVariable UUID memberId
    ) {
        UUID userId = generalService.getIdUserFromSession();
        workspaceFacade.removeMember(workspaceId, memberId, userId, false);
    }

    @DeleteMapping("/admin/{workspaceId}/members/{memberId}")
    public void adminRemoveMember(
        @PathVariable UUID workspaceId,
        @PathVariable UUID memberId
    ) {
        UUID userId = generalService.getIdUserFromSession();
        workspaceFacade.removeMember(workspaceId, memberId, userId, true);
    }


    // ========== GESTIÓN DE INVITACIONES ==========

    @GetMapping("/my-invitations")
    public List< WorkspaceInvitationResponse > myInvitations() {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.myInvitations(  userId);
    }

    @PatchMapping("/{workspaceId}/invitations/{invitationId}/respond")
    public WorkspaceInvitationResponse respondToInvitation(
        @PathVariable UUID workspaceId,
        @PathVariable UUID invitationId,
        @Valid @RequestBody RespondInvitationRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.respondToInvitation(invitationId, request.response(), userId);
    }

    // ========== RESTAURACIÓN DE WORKSPACE ==========

    @PatchMapping("/admin/{workspaceId}/restore")
    public WorkspaceResponse adminRestore(
        @PathVariable UUID workspaceId
    ) {
        return workspaceFacade.restoreWorkspace(workspaceId);
    }

    @PatchMapping("/{workspaceId}/restore")
    public WorkspaceResponse restore(
        @PathVariable UUID workspaceId
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.restoreWorkspace(workspaceId, userId);
    }

}
