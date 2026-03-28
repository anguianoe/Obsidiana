package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public sealed interface WikiPageRevisionViewResponse permits WikiPageRevisionResponse, WikiPageRevisionEncryptedResponse {
    UUID id();
    UUID pageId();
    Integer revisionNumber();
    String titleSnapshot();
    Boolean isEncrypted();
    Boolean isPinned();
    UUID createdBy();
    OffsetDateTime createdAt();
}

