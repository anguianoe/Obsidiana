package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceTagUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTagAssignmentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceTagResponse;
import com.nexcoyo.knowledge.obsidiana.facade.TagFacade;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagFacade tagFacade;

    @GetMapping("/workspace/{workspaceId}")
    public List< WorkspaceTagResponse > activeTags( @PathVariable UUID workspaceId) {
        return tagFacade.activeTags(workspaceId);
    }

    @PostMapping
    public WorkspaceTagResponse create(@Valid @RequestBody WorkspaceTagUpsertRequest request) {
        return tagFacade.save(request);
    }

    @PutMapping("/{tagId}")
    public WorkspaceTagResponse update(@PathVariable UUID tagId, @Valid @RequestBody WorkspaceTagUpsertRequest request) {
        return tagFacade.save(new WorkspaceTagUpsertRequest(tagId, request.workspaceId(), request.name(), request.tagStatus(), request.createdBy()));
    }

    @PostMapping("/assign")
    public PageTagAssignmentResponse assign( @Valid @RequestBody AssignTagRequest request) {
        return tagFacade.assign(request);
    }

    @GetMapping("/assignments")
    public List<PageTagAssignmentResponse> assignments(@RequestParam UUID pageId, @RequestParam UUID workspaceId) {
        return tagFacade.assignments(pageId, workspaceId);
    }
}
