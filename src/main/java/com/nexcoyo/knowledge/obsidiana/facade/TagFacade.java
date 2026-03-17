package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.dto.request.AssignTagRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WorkspaceTagUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTagAssignmentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceTagResponse;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagFacade {

    private final TagService tagService;
    private final EntityReferenceResolver refs;

    public List< WorkspaceTagResponse > activeTags( UUID workspaceId) {
        return tagService.getActiveTags(workspaceId).stream().map( ApiMapper::toResponse).toList();
    }

    public WorkspaceTagResponse save( WorkspaceTagUpsertRequest request) {
        WorkspaceTag entity = new WorkspaceTag();
        if (request.id() != null) {
            entity.setId(request.id());
        }
        entity.setWorkspace(refs.workspace(request.workspaceId()));
        entity.setName(request.name());
        entity.setTagStatus(request.tagStatus());
        entity.setCreatedBy(refs.user(request.createdBy()));
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(OffsetDateTime.now());
        }
        return ApiMapper.toResponse(tagService.saveTag(entity));
    }

    public PageTagAssignmentResponse assign( AssignTagRequest request) {
        return ApiMapper.toResponse(tagService.assignTag(request.pageId(), request.workspaceId(), request.tagId(), request.actorUserId()));
    }

    public List<PageTagAssignmentResponse> assignments(UUID pageId, UUID workspaceId) {
        return tagService.getPageAssignments(pageId, workspaceId).stream().map(ApiMapper::toResponse).toList();
    }
}
