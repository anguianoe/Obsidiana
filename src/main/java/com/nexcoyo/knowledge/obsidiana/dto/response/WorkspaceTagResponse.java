package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.TagStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceTagResponse( UUID id, UUID workspaceId, String name, TagStatus tagStatus, UUID createdBy, OffsetDateTime createdAt) {}
