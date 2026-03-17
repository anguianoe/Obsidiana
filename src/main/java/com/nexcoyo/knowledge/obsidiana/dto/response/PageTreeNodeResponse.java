package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.util.UUID;

public record PageTreeNodeResponse(UUID pageId, String title, String slug, Integer sortOrder, Long childCount) {}
