package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record WikiPageRevisionCreateRequest(
    UUID id,
    @NotNull UUID pageId,
    @NotNull Integer revisionNumber,
    @NotBlank @Size(max = 255) String titleSnapshot,
    @NotNull EditorType editorType,
    String contentHtml,
    String contentText,
    @Size(max = 500) String changeSummary,
    @NotNull Boolean isEncrypted,
    String contentIv,
    String contentAuthTag,
    String encryptionKdf,
    @NotNull Boolean isPinned,
    @NotNull UUID createdBy,
    Boolean updatePagePointer
) {}
