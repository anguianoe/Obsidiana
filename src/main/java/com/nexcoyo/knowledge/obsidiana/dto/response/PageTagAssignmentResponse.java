package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageTagAssignmentResponse( UUID id, UUID pageId, UUID workspaceId, UUID tagId, AssignmentStatus assignmentStatus, UUID createdBy, OffsetDateTime createdAt) {}
