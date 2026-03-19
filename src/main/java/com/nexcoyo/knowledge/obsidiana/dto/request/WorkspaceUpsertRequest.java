package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record WorkspaceUpsertRequest(
    UUID id,
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Size(max = 160) String slug,
    @NotNull WorkspaceKind kind,
    @Size(max = 4000) String description
) {}
