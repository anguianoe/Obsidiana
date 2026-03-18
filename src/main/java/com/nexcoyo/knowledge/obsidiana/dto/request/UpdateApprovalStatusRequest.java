package com.nexcoyo.knowledge.obsidiana.dto.request;

import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateApprovalStatusRequest(
    @NotNull ApprovalStatus approvalStatus,
    UUID approvedBy
) {}

