package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceTagUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTagAssignmentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceTagResponse;
import com.nexcoyo.knowledge.obsidiana.facade.TagFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TagAdminController
{

    private final TagFacade tagFacade;
    private final GeneralService generalService;

    @GetMapping("/workspace/{workspaceId}")
    public List< WorkspaceTagResponse > activeTags( @PathVariable UUID workspaceId) {
        return tagFacade.activeTags(workspaceId);
    }

    @PostMapping
    public WorkspaceTagResponse create(@Valid @RequestBody WorkspaceTagUpsertRequest request) {
        UUID actorId = generalService.getIdUserFromSession();
        return tagFacade.save(new WorkspaceTagUpsertRequest(
            request.id(),
            request.workspaceId(),
            request.name(),
            request.tagStatus(),
            actorId
        ));
    }

    @PutMapping("/{tagId}")
    public WorkspaceTagResponse update(@PathVariable UUID tagId, @Valid @RequestBody WorkspaceTagUpsertRequest request) {
        UUID actorId = generalService.getIdUserFromSession();
        return tagFacade.save(new WorkspaceTagUpsertRequest(tagId, request.workspaceId(), request.name(), request.tagStatus(), actorId));
    }

    @PostMapping("/assign")
    public PageTagAssignmentResponse assign( @Valid @RequestBody AssignTagRequest request) {
        UUID actorId = generalService.getIdUserFromSession();
        return tagFacade.assign(new AssignTagRequest(request.pageId(), request.workspaceId(), request.tagId(), actorId));
    }

    @GetMapping("/assignments")
    public List<PageTagAssignmentResponse> assignments(@RequestParam UUID pageId, @RequestParam UUID workspaceId) {
        return tagFacade.assignments(pageId, workspaceId);
    }
}
