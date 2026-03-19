package com.nexcoyo.knowledge.obsidiana.projection;

import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface WorkspaceSummaryProjection {
    UUID getWorkspaceId();
    String getWorkspaceName();
    String getWorkspaceSlug();
    WorkspaceKind getKind();
    WorkspaceStatus getStatus();
    ApprovalStatus getApprovalStatus();
    WorkspaceRole getMembershipRole();
    Instant getJoinedAt();
}
