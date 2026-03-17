package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record WorkspaceUpsertRequest(
    UUID id,
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Size(max = 160) String slug,
    @NotNull WorkspaceKind kind,
    @NotNull WorkspaceStatus status,
    @NotNull ApprovalStatus approvalStatus,
    @NotNull UUID createdBy,
    UUID approvedBy,
    @Size(max = 4000) String description
) {}
