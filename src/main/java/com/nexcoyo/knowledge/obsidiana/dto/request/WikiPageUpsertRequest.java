package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditMode;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record WikiPageUpsertRequest(
    UUID id,
    UUID publicUuid,
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 180) String slug,
    @NotNull EditMode editMode,
    @NotNull PageStatus pageStatus,
    @NotNull Boolean isEncrypted,
    @NotNull Boolean isPublicable,
    UUID currentRevisionId
) {}
