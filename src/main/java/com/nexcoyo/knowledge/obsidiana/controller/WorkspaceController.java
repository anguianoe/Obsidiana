package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.InviteMemberRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.RespondInvitationRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateMemberRoleRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.facade.WorkspaceFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
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
    private final GeneralService generalService;

    @PostMapping
    public WorkspaceResponse create(@Valid @RequestBody WorkspaceUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.save(request, userId, false);
    }

    @GetMapping("/my-invitations")
    public List< WorkspaceInvitationResponse > myInvitations() {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.myInvitations( userId);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse getById(@PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.getById(workspaceId, userId);
    }

    @GetMapping("/accessible")
    public List< WorkspaceSummaryResponse > accessible( ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.accessible(userId);
    }

    @GetMapping("/{workspaceId}/members")
    public List< WorkspaceMembershipResponse > activeMembers( @PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.activeMembers(workspaceId, userId);
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
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.pendingInvitations(workspaceId, userId);
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse update(@PathVariable UUID workspaceId, @Valid @RequestBody WorkspaceUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.save(new WorkspaceUpsertRequest(
                workspaceId, request.name(), request.slug(), request.kind(), request.description()
        ), userId,false);
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

    @PostMapping("/{workspaceId}/members")
    public WorkspaceInvitationResponse inviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.inviteMember(workspaceId, request.userId(), request.role(), userId, false);
    }

    @PatchMapping("/{workspaceId}/status/inactive")
    public WorkspaceResponse setInactive(@PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.setInactive(workspaceId, userId);
    }

    @PatchMapping("/{workspaceId}/restore")
    public WorkspaceResponse restore(
            @PathVariable UUID workspaceId
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.restoreWorkspace(workspaceId, userId);
    }

    @PatchMapping("/invitations/{invitationId}/respond")
    public WorkspaceInvitationResponse respondToInvitation(
        @PathVariable UUID invitationId,
        @Valid @RequestBody RespondInvitationRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return workspaceFacade.respondToInvitation(invitationId, request.response(), userId);
    }

    @DeleteMapping("/{workspaceId}/members/{memberId}")
    public void removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId
    ) {
        UUID userId = generalService.getIdUserFromSession();
        workspaceFacade.removeMember(workspaceId, memberId, userId, false);
    }

    @DeleteMapping("/{workspaceId}")
    public void delete(@PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        workspaceFacade.delete(workspaceId, userId);
    }
}
