package com.nexcoyo.knowledge.obsidiana.service.dto.search;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditMode;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WikiPageSearchCriteria {
    private UUID ownerUserId;
    private UUID workspaceId;
    private UUID tagId;
    private UUID parentPageId;
    private String text;
    private PageStatus pageStatus;
    private EditMode editMode;
    private Boolean encrypted;
    private Boolean publicable;
    private Boolean onlyRootPages;
}
