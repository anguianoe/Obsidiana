package com.nexcoyo.knowledge.obsidiana.service.dto.search;


import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetSearchCriteria {
    private UUID uploadedBy;
    private AssetType assetType;
    private AssetStatus status;
    private String fileNameOrObjectKey;
    private String mimeTypePrefix;
    private Boolean onlyOrphans;
}
