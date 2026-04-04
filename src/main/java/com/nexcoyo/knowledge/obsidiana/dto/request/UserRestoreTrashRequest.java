package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.Size;

public record UserRestoreTrashRequest(@Size(max = 255) String reason) {}

