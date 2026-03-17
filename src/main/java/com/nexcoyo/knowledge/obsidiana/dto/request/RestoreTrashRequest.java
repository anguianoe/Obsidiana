package com.nexcoyo.knowledge.obsidiana.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RestoreTrashRequest(@NotNull UUID restoredBy, @Size(max = 255) String reason) {}
