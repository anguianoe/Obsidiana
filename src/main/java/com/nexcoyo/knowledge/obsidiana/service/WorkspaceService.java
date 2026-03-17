package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.projection.WorkspaceSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkspaceService {
    Page< Workspace > search( WorkspaceSearchCriteria criteria, Pageable pageable);
    List< WorkspaceSummaryProjection > findAccessibleSummaries( UUID userId);
    Workspace getRequired(UUID workspaceId);
    Workspace save(Workspace workspace);
    List< WorkspaceMembership > getActiveMembers( UUID workspaceId);
    List< WorkspaceInvitation > getPendingInvitations( UUID workspaceId);
}
