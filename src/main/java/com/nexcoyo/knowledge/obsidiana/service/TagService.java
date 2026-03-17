package com.nexcoyo.knowledge.obsidiana.service;

import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;

import java.util.List;
import java.util.UUID;

public interface TagService {
    List< WorkspaceTag > getActiveTags( UUID workspaceId);
    WorkspaceTag saveTag(WorkspaceTag workspaceTag);
    PageTagAssignment assignTag(UUID pageId, UUID workspaceId, UUID tagId, UUID actorUserId);
    List< PageTagAssignment > getPageAssignments( UUID pageId, UUID workspaceId);
}
