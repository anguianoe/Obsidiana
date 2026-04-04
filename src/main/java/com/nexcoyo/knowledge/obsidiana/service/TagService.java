package com.nexcoyo.knowledge.obsidiana.service;

import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;

import java.util.List;
import java.util.UUID;

public interface TagService {
    List< WorkspaceTag > getActiveTags( UUID workspaceId);
    List<WorkspaceTag> getActiveTags(UUID workspaceId, UUID userId);
    WorkspaceTag saveTag(WorkspaceTag workspaceTag);
    WorkspaceTag saveTag(WorkspaceTag workspaceTag, UUID userId);
    PageTagAssignment assignTag(UUID pageId, UUID workspaceId, UUID tagId, UUID actorUserId);
    PageTagAssignment assignTag(UUID pageId, UUID workspaceId, UUID tagId, UUID actorUserId, UUID userId);
    List< PageTagAssignment > getPageAssignments( UUID pageId, UUID workspaceId);
    List<PageTagAssignment> getPageAssignments(UUID pageId, UUID workspaceId, UUID userId);
}
