package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceTagUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTagAssignmentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceTagResponse;
import com.nexcoyo.knowledge.obsidiana.facade.TagFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TagController {

    private final TagFacade tagFacade;
    private final GeneralService generalService;

    @GetMapping("/workspace/{workspaceId}")
    public List< WorkspaceTagResponse > activeTags( @PathVariable UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return tagFacade.activeTagsForUser(workspaceId, userId);
    }

    @PostMapping
    public WorkspaceTagResponse create(@Valid @RequestBody WorkspaceTagUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return tagFacade.saveForUser(request, userId);
    }

    @PutMapping("/{tagId}")
    public WorkspaceTagResponse update(@PathVariable UUID tagId, @Valid @RequestBody WorkspaceTagUpsertRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return tagFacade.saveForUser(new WorkspaceTagUpsertRequest(tagId, request.workspaceId(), request.name(), request.tagStatus(), userId), userId);
    }

    @PostMapping("/assign")
    public PageTagAssignmentResponse assign( @Valid @RequestBody AssignTagRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return tagFacade.assignForUser(request, userId);
    }

    @GetMapping("/assignments")
    public List<PageTagAssignmentResponse> assignments(@RequestParam UUID pageId, @RequestParam UUID workspaceId) {
        UUID userId = generalService.getIdUserFromSession();
        return tagFacade.assignmentsForUser(pageId, workspaceId, userId);
    }
}
