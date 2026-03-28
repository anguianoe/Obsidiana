package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WikiPageRevisionEncryptedResponse(
    UUID id,
    UUID pageId,
    Integer revisionNumber,
    String titleSnapshot,
    EditorType editorType,
    String changeSummary,
    Boolean isEncrypted,
    String contentCiphertext,
    String contentIv,
    String contentAuthTag,
    String encryptionKdf,
    Boolean isPinned,
    UUID createdBy,
    OffsetDateTime createdAt
) implements WikiPageRevisionViewResponse {}

