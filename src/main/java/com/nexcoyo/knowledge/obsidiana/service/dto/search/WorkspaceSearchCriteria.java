package com.nexcoyo.knowledge.obsidiana.service.dto.search;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceSearchCriteria {
    private UUID userId;
    private String nameOrSlug;
    private WorkspaceKind kind;
    private WorkspaceStatus status;
    private ApprovalStatus approvalStatus;
    private Boolean onlyOwned;
    private Boolean onlyMember;
}
