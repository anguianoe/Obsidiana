package com.nexcoyo.knowledge.obsidiana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "page_workspace_link", schema = "obsidiana")
public class PageWorkspaceLink extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private WikiPage page;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "linked_at", nullable = false)
    private OffsetDateTime linkedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_by")
    private AppUser linkedBy;
}
