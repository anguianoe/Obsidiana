package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PublishPageRequest(
    UUID id,
    @NotNull UUID pageId,
    @NotNull UUID revisionId,
    @NotBlank @Size(max = 180) String publicSlug,
    @NotBlank @Size(max = 255) String publicTitle,
    @NotBlank String publicHtml,
    @NotNull PublicationStatus publicationStatus,
    @NotNull UUID publishedBy
) {}
