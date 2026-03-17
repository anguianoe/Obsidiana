package com.nexcoyo.knowledge.obsidiana.entity;


import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "workspace_membership", schema = "obsidiana")
public class WorkspaceMembership extends AuditableTimestampsEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private WorkspaceRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MembershipStatus status;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;
}
