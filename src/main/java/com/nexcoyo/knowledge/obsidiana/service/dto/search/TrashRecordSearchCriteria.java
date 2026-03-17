package com.nexcoyo.knowledge.obsidiana.service.dto.search;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrashRecordSearchCriteria {
    private UUID workspaceId;
    private UUID pageId;
    private UUID assetId;
    private UUID deletedBy;
    private TrashEntityType entityType;
    private TrashStatus status;
    private Boolean overdue;
}
