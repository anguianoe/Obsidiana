package com.nexcoyo.knowledge.obsidiana.dto.response;


import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceSummaryResponse(
    UUID workspaceId,
    String workspaceName,
    String workspaceSlug,
    WorkspaceKind kind,
    WorkspaceStatus status,
    ApprovalStatus approvalStatus,
    WorkspaceRole membershipRole,
    Instant joinedAt
) {}
