package com.nexcoyo.knowledge.obsidiana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_page_nav_preference", schema = "obsidiana")
public class UserPageNavPreference extends AuditableTimestampsEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_page_id")
    private WikiPage parentPage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private WikiPage page;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @Column(name = "collapsed", nullable = false)
    private Boolean collapsed;
}
