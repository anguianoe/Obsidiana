package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.TagStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record WorkspaceTagUpsertRequest(
    UUID id,
    @NotNull UUID workspaceId,
    @NotBlank @Size(max = 120) String name,
    @NotNull TagStatus tagStatus,
    UUID createdBy
) {}
