package com.nexcoyo.knowledge.obsidiana.dto.response;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WikiPageRevisionResponse(
    UUID id, UUID pageId, Integer revisionNumber, String titleSnapshot, EditorType editorType,
    String contentHtml, String contentText, String changeSummary, Boolean isEncrypted,
    Boolean isPinned,
    UUID createdBy, OffsetDateTime createdAt
) implements WikiPageRevisionViewResponse {}
